/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.either
import arrow.core.right
import io.github.bommbomm34.intervirt.core.api.impl.ContainerSshClient
import io.github.bommbomm34.intervirt.core.api.impl.VirtualContainerIOClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl.ActualDockerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl.VirtualDockerManager
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.modify
import io.github.bommbomm34.intervirt.core.util.*
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.lastResult
import io.github.bommbomm34.intervirt.core.util.ext.toReadableImage
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class DeviceManager(
    private val guestManager: GuestManager,
    private val qemuClient: QemuClient,
    private val executor: Executor,
    private val fileManager: FileManager,
    private val appEnv: AppEnv,
    project: Atomic<Project>,
) : AsyncCloseable {
    private val logger = appEnv.getLogger(DeviceManager::class)
    private val virtualContainerIO = appEnv.VIRTUAL_CONTAINER_IO
    private val virtualContainerIOPort = appEnv.VIRTUAL_CONTAINER_IO_PORT
    private val wipeVirtualOnClose = appEnv.WIPE_VIRTUAL_ON_CLOSE
    private val dockerHostOverride = appEnv.OVERRIDE_DOCKER_HOST.ifBlank { null }
    private val containerIOClients = ConcurrentHashMap<String, ContainerIOClient>()
    private val dockerManagers = ConcurrentHashMap<String, DockerManager>()
    private val intervirtOSClients = ConcurrentHashMap<String, IntervirtOSClient>()
    private val _project = project
    private val project by _project

    context(_: Raise<Failure>)
    suspend fun addComputer(name: String? = null, x: Int, y: Int, image: String): Device.Computer {
        val id = generateID("computer")
        val device = Device.Computer(
            id = id,
            image = image,
            name = name ?: id,
            x = x,
            y = y,
            ipv4 = project.generateIpv4(guestManager.getInfo().ipv4Subnet),
            ipv6 = project.generateIpv6(guestManager.getInfo().ipv6Subnet),
            mac = project.generateMac(),
            internetEnabled = false,
            portForwardings = listOf(),
        )
        return addComputer(device)
    }

    context(_: Raise<Failure>)
    suspend fun addComputer(device: Device.Computer): Device.Computer {
        validateComputer(device)
        logger.debug { "Adding device $device" }
        Project.devices.modify(_project) { it + device }
        guestManager.addContainer(
            id = device.id,
            ipv4 = device.ipv4,
            ipv6 = device.ipv6,
            mac = device.mac,
            internet = false,
            image = device.image,
        ).lastResult() // TODO: Propagate flow
        logger.info { "Added device $device" }
        return device
    }

    fun addSwitch(name: String? = null, x: Int, y: Int): Device.Switch {
        val id = generateID("switch")
        val device = Device.Switch(
            id = id,
            name = name ?: id,
            x = x,
            y = y,
        )
        logger.debug { "Adding device $device" }
        Project.devices.modify(_project) { it + device }
        logger.info { "Added device $device" }
        return device
    }

    context(_: Raise<Failure>)
    suspend fun removeDevice(device: Device) {
        device.requireExists()
        logger.debug { "Removing device $device" }
        Project.connections.modify(_project) { project ->
            project.filterNot { it.containsDevice(device) }
        }
        Project.devices.modify(_project) { it - device }
        // Close services
        intervirtOSClients[device.id]?.close()
        intervirtOSClients.remove(device.id)
        dockerManagers[device.id]?.close()
        dockerManagers.remove(device.id)
        containerIOClients[device.id]?.close()
        containerIOClients.remove(device.id)
        if (device is Device.Computer) {
            guestManager.removeContainer(device.id)
        }
        logger.info { "Removed device $device" }
    }

    context(_: Raise<Failure>)
    suspend fun connectDevice(device1: Device, device2: Device) {
        device1.requireExists()
        device2.requireExists()
        logger.debug { "Connecting device $device1 to $device2" }
        val conn = device1 connect device2
        Project.connections.modify(_project) { it + conn }
        val networks = project.resolveNetworks(conn)
        guestManager.addNetworks(networks)
        logger.info { "Connected device $device1 to $device2" }
    }

    context(_: Raise<Failure>)
    suspend fun disconnectDevice(device1: Device, device2: Device) {
        device1.requireExists()
        device2.requireExists()
        logger.debug { "Disconnecting device $device1 to $device2" }
        val conn = device1 connect device2
        Project.connections.modify(_project) { it - conn }
        val networks = project.resolveNetworks(conn)
        networks.forEach { (name, network) ->
            network.forEach {
                guestManager.disconnect(it, name)
            }
        }
        clearUnusedNetworks()
        logger.info { "Disconnected device $device1 to $device2" }
    }

    context(_: Raise<Failure>)
    suspend fun setIpv4(device: Device.Computer, ipv4: String) {
        device.requireExists()
        logger.debug { "Setting $ipv4 of $device" }

        _project.modifyDevice(device) { device.copy(ipv4 = ipv4) }
        return guestManager.setIpv4(device.id, ipv4).also {
            logger.info { "Set $ipv4 of $device" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun setIpv6(device: Device.Computer, ipv6: String) {
        device.requireExists()
        logger.debug { "Setting $ipv6 of $device" }
        _project.modifyDevice(device) { device.copy(ipv6 = ipv6) }
        return guestManager.setIpv6(device.id, ipv6).also {
            logger.info { "Set $ipv6 of $device" }
        }
    }

    context(_: Raise<Failure>)
    fun setName(device: Device, name: String) {
        device.requireExists()
        _project.modifyDevice(device) { Device.name.set(device, name) }
        logger.debug { "Set name of ${device.name} to $name" }
    }

    context(_: Raise<Failure>)
    suspend fun setInternetEnabled(device: Device.Computer, enabled: Boolean) {
        device.requireExists()
        logger.debug { "Setting internet enabled of ${device.id} to $enabled" }
        _project.modifyDevice(device) { device.copy(internetEnabled = enabled) }
        return guestManager.setInternetAccess(device.id, enabled).also {
            logger.info { "Set internet enabled of ${device.id} to $enabled" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun start(computer: Device.Computer) {
        computer.requireExists()
        logger.debug { "Starting ${computer.id}" }
        return guestManager.startContainer(computer.id).also {
            logger.info { "Started ${computer.id}" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun stop(computer: Device.Computer) {
        computer.requireExists()
        logger.debug { "Stopping ${computer.id}" }
        return guestManager.stopContainer(computer.id).also {
            logger.info { "Stopped ${computer.id}" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun addPortForwarding(
        device: Device.Computer,
        portForwarding: PortForwarding,
    ) {
        device.requireExists()
        portForwarding.requireValid()
        logger.debug { "Add port forwarding $portForwarding for ${device.id}" }
        _project.modifyDevice(device) { _ ->
            Device.Computer.portForwardings.modify(device) { it + portForwarding }
        }
        if (!virtualContainerIO) qemuClient.addPortForwarding(
            protocol = portForwarding.protocol,
            externalPort = portForwarding.externalPort,
            internalPort = portForwarding.internalPort,
        )
        guestManager.addPortForwarding(
            id = device.id,
            internalPort = portForwarding.internalPort,
            externalPort = portForwarding.externalPort,
            protocol = portForwarding.protocol,
        ).also {
            logger.info { "Added port forwarding $portForwarding for ${device.id}" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun removePortForwarding(
        externalPort: Int,
        protocol: String,
    ) {
        require(externalPort.isValidPort()) { "External port $externalPort is not valid!" }
        require(protocol.isValidProtocol()) { "Protocol $protocol is not valid!" }
        logger.debug { "Remove port forwarding of $externalPort" }
        val device = project.devices.firstOrNull { device ->
            if (device is Device.Computer) {
                Device.Computer.portForwardings.modify(device) { portForwardings ->
                    portForwardings.filterNot {
                        it.externalPort == externalPort && it.protocol == protocol
                    }
                }
                true
            } else false
        }
        requireNotNull(device) { "There was no device binding external port $externalPort on protocol $protocol" }
        if (!virtualContainerIO) qemuClient.removePortForwarding(
            protocol = protocol,
            externalPort = externalPort,
        )
        guestManager.removePortForwarding(device.id, externalPort, protocol).also {
            logger.info { "Removed port forwarding of $externalPort" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun getIOClient(computer: Device.Computer): ContainerIOClient {
        computer.requireExists()
        logger.debug { "Retrieving IO client of ${computer.id}" }
        val cached = containerIOClients[computer.id]
        if (cached != null) return cached
        return if (virtualContainerIO) initVirtualIOClient(computer) else initSshClient(computer)
    }


    context(_: Raise<Failure>)
    suspend fun initSshClient(computer: Device.Computer): ContainerSshClient {
        computer.requireExists()
        val port = getFreePort()
        logger.debug { "Initializing SSH client for ${computer.id} on port $port" }
        return addPortForwarding(
            device = computer,
            portForwarding = PortForwarding(
                protocol = "tcp",
                internalPort = 22,
                externalPort = port,
                hidden = true,
            ),
        ).let {
            val sshClient = ContainerSshClient(appEnv, port, this, computer.id)
            sshClient.init()
            containerIOClients[computer.id] = sshClient
            logger.info { "Initialized SSH client for ${computer.id}" }
            sshClient
        }
    }

    fun initVirtualIOClient(computer: Device.Computer): VirtualContainerIOClient {
        computer.requireExists()
        logger.debug { "Initializing virtual container IO client for ${computer.id}" }
        val client = VirtualContainerIOClient(computer.id, wipeVirtualOnClose, executor, fileManager)
        containerIOClients[computer.id] = client
        logger.debug { "Initialized virtual container IO client for ${computer.id}" }
        return client
    }

    context(_: Raise<Failure>)
    suspend fun getIntervirtOSClient(computer: Device.Computer): IntervirtOSClient {
        computer.requireExists()
        logger.debug { "Retrieving IntervirtOSClient of ${computer.id}" }
        intervirtOSClients[computer.id]?.let { return it }
        val ioClient = getIOClient(computer)
        val osClient = IntervirtOSClient(
            IntervirtOSClient.Client(
                appEnv = appEnv,
                computer = computer,
                ioClient = ioClient,
                docker = getDockerManager(computer, ioClient),
            ),
        )
        osClient.init()
        intervirtOSClients[computer.id] = osClient
        logger.debug { "Retrieved IntervirtOSClient of ${computer.id}" }
        return osClient
    }

    context(_: Raise<Failure>)
    fun getDockerManager(
        computer: Device.Computer,
        ioClient: ContainerIOClient,
    ): DockerManager {
        computer.requireExists()
        logger.debug { "Initializing DockerManager for ${computer.id}" }
        dockerManagers[computer.id]?.let { return it }
        val sshClient = ioClient as? ContainerSshClient
        val dockerManager = when (appEnv.VIRTUAL_DOCKER_MANAGER) {
            true -> VirtualDockerManager()
            false -> ActualDockerManager(
                appEnv,
                dockerHostOverride ?: "ssh://127.0.0.1:${sshClient?.port ?: virtualContainerIOPort}",
            )
        }
        dockerManagers[computer.id] = dockerManager
        logger.debug { "Initialized DockerManager for ${computer.id}" }
        return dockerManager
    }

    context(_: Raise<Failure>)
    private suspend fun clearUnusedNetworks() {
        guestManager.getNetworks().let { networks ->
            logger.debug { "Clearing unused networks" }
            networks
                .filter { it.value.isEmpty() }
                .forEach {
                    guestManager.removeNetwork(it.key)
                }
            logger.debug { "Cleared unused networks" }
        }
    }

    private fun generateID(prefix: String): String {
        while (true) {
            val id = prefix + "-" + Random.nextInt(999999)
            if (project.devices.all { it.id != id }) return id
        }
    }

    private fun validateComputer(computer: Device.Computer) {
        // Validate image
        requireNotNull(computer.image.toReadableImage()) { "Invalid image: ${computer.image}" }
        // Validate IP
        require(computer.ipv4.validateIpv4()) { "IPv4 address is invalid: ${computer.ipv4}" }
        require(computer.ipv6.validateIpv6()) { "IPv6 address is invalid: ${computer.ipv6}" }
        // Validate MAC
        require(computer.mac.validateMac()) { "MAC address is invalid: ${computer.mac}" }
        // Validate port forwardings
        computer.portForwardings.forEach { it.requireValid() }
    }

    private fun Device.exists() = project.devices.any { it.id == id }

    private fun Device.requireExists() = require(exists()) { "Device $id does not exist!" }

    private fun PortForwarding.requireValid() = require(validate()) { "Port forwarding is invalid: $this" }

    context(_: Raise<Failure>)
    override suspend fun close() {
        logger.debug { "Closing DeviceManager" }
        intervirtOSClients.forEach { (_, client) -> client.close() }
        dockerManagers.forEach { (_, manager) -> manager.close() }
        containerIOClients.forEach { (_, client) -> client.close() }
        logger.debug { "Closed DeviceManager" }
    }
}

fun getFreePort() = ServerSocket(0).use { it.localPort }
fun Int.isValidPort() = this in 1..65535

fun String.isValidProtocol() = this == "tcp" || this == "udp"

fun PortForwarding.validate(): Boolean {
    return externalPort.isValidPort() && internalPort.isValidPort() && protocol.isValidProtocol()
}

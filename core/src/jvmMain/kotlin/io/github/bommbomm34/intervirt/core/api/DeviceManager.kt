/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.impl.ContainerSshClient
import io.github.bommbomm34.intervirt.core.api.impl.VirtualContainerIOClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl.ActualDockerManager
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.util.*
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.lastResult
import io.github.bommbomm34.intervirt.core.util.ext.runSuspendingCatching
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
    private var project by project

    suspend fun addComputer(name: String? = null, x: Int, y: Int, image: String): Result<Device.Computer> {
        val id = generateID("computer")
        val device = Device.Computer(
            id = id,
            image = image,
            name = name ?: id,
            x = x,
            y = y,
            ipv4 = project.generateIpv4(),
            ipv6 = project.generateIpv6(),
            mac = project.generateMac(),
            internetEnabled = false,
            portForwardings = listOf(),
        )
        return addComputer(device)
    }

    suspend fun addComputer(device: Device.Computer): Result<Device.Computer> = runSuspendingCatching {
        validateComputer(device)
        logger.debug { "Adding device $device" }
        project = Project.devices.modify(project) { it + device }
        guestManager.addContainer(
            id = device.id,
            ipv4 = device.ipv4,
            ipv6 = device.ipv6,
            mac = device.mac,
            internet = false,
            image = device.image,
        ).lastResult().getOrThrow() // TODO: Propagate flow
        logger.info { "Added device $device" }
        device
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
        project = Project.devices.modify(project) { it + device }
        logger.info { "Added device $device" }
        return device
    }

    suspend fun removeDevice(device: Device): Result<Unit> = runSuspendingCatching {
        device.requireExists()
        logger.debug { "Removing device $device" }
        project = Project.connections.modify(project) { project ->
            project.filterNot { it.containsDevice(device) }
        }
        project = Project.devices.modify(project) { it - device }
        // Close services
        intervirtOSClients[device.id]?.close()?.getOrThrow()
        intervirtOSClients.remove(device.id)
        dockerManagers[device.id]?.close()?.getOrThrow()
        dockerManagers.remove(device.id)
        containerIOClients[device.id]?.close()?.getOrThrow()
        containerIOClients.remove(device.id)
        if (device is Device.Computer) {
            guestManager.removeContainer(device.id).getOrThrow()
        }
        logger.info { "Removed device $device" }
    }

    suspend fun connectDevice(device1: Device, device2: Device): Result<Unit> = runSuspendingCatching {
        device1.requireExists()
        device2.requireExists()
        logger.debug { "Connecting device $device1 to $device2" }
        val conn = device1 connect device2
        project = Project.connections.modify(project) { it + conn }
        val networks = project.resolveNetworks(conn)
        guestManager.addNetworks(networks).getOrThrow()
        logger.info { "Connected device $device1 to $device2" }
    }

    suspend fun disconnectDevice(device1: Device, device2: Device): Result<Unit> = runSuspendingCatching {
        device1.requireExists()
        device2.requireExists()
        logger.debug { "Disconnecting device $device1 to $device2" }
        val conn = device1 connect device2
        project = Project.connections.modify(project) { it - conn }
        val networks = project.resolveNetworks(conn)
        networks.forEach { (name, network) ->
            network.forEach {
                guestManager.disconnect(it, name).getOrThrow()
            }
        }
        clearUnusedNetworks()
        logger.info { "Disconnected device $device1 to $device2" }
    }

    suspend fun setIpv4(device: Device.Computer, ipv4: String): Result<Unit> {
        device.requireExists()
        logger.debug { "Setting $ipv4 of $device" }

        project = project.modifyDevice(device) { device.copy(ipv4 = ipv4) }
        return guestManager.setIpv4(device.id, ipv4).map {
            logger.info { "Set $ipv4 of $device" }
        }
    }

    suspend fun setIpv6(device: Device.Computer, ipv6: String): Result<Unit> {
        device.requireExists()
        logger.debug { "Setting $ipv6 of $device" }
        project = project.modifyDevice(device) { device.copy(ipv6 = ipv6) }
        return guestManager.setIpv6(device.id, ipv6).map {
            logger.info { "Set $ipv6 of $device" }
        }
    }

    fun setName(device: Device, name: String) {
        device.requireExists()
        project = project.modifyDevice(device) { Device.name.set(device, name) }
        logger.debug { "Set name of ${device.name} to $name" }
    }

    suspend fun setInternetEnabled(device: Device.Computer, enabled: Boolean): Result<Unit> {
        device.requireExists()
        logger.debug { "Setting internet enabled of ${device.id} to $enabled" }
        project = project.modifyDevice(device) { device.copy(internetEnabled = enabled) }
        return guestManager.setInternetAccess(device.id, enabled).map {
            logger.info { "Set internet enabled of ${device.id} to $enabled" }
        }
    }

    suspend fun start(computer: Device.Computer): Result<Unit> {
        computer.requireExists()
        logger.debug { "Starting ${computer.id}" }
        return guestManager.startContainer(computer.id).map {
            logger.info { "Started ${computer.id}" }
        }
    }

    suspend fun stop(computer: Device.Computer): Result<Unit> {
        computer.requireExists()
        logger.debug { "Stopping ${computer.id}" }
        return guestManager.stopContainer(computer.id).map {
            logger.info { "Stopped ${computer.id}" }
        }
    }

    suspend fun addPortForwarding(
        device: Device.Computer,
        portForwarding: PortForwarding,
    ): Result<Unit> {
        device.requireExists()
        portForwarding.requireValid()
        logger.debug { "Add port forwarding $portForwarding for ${device.id}" }
        project = project.modifyDevice(device) { _ ->
            Device.Computer.portForwardings.modify(device) { it + portForwarding }
        }
        if (!virtualContainerIO) qemuClient.addPortForwarding(
            protocol = portForwarding.protocol,
            externalPort = portForwarding.externalPort,
            internalPort = portForwarding.internalPort,
        ).onFailure { return Result.failure(it) }
        return guestManager.addPortForwarding(
            id = device.id,
            internalPort = portForwarding.internalPort,
            externalPort = portForwarding.externalPort,
            protocol = portForwarding.protocol,
        ).map {
            logger.info { "Added port forwarding $portForwarding for ${device.id}" }
        }
    }

    suspend fun removePortForwarding(
        externalPort: Int,
        protocol: String,
    ): Result<Unit> {
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
        ).onFailure { return Result.failure(it) }
        return guestManager.removePortForwarding(device.id, externalPort, protocol).map {
            logger.info { "Removed port forwarding of $externalPort" }
        }
    }

    suspend fun getIOClient(computer: Device.Computer): Result<ContainerIOClient> {
        computer.requireExists()
        logger.debug { "Retrieving IO client of ${computer.id}" }
        val cached = containerIOClients[computer.id]?.let { Result.success(it) }
        if (cached != null) return cached
        return if (virtualContainerIO) Result.success(initVirtualIOClient(computer)) else initSshClient(computer)
    }


    suspend fun initSshClient(computer: Device.Computer): Result<ContainerSshClient> {
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
        ).mapCatching {
            val sshClient = ContainerSshClient(appEnv, port, this, computer.id)
            sshClient.init().getOrThrow()
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

    suspend fun getIntervirtOSClient(computer: Device.Computer) = runSuspendingCatching {
        computer.requireExists()
        logger.debug { "Retrieving IntervirtOSClient of ${computer.id}" }
        intervirtOSClients[computer.id]?.let { return@runSuspendingCatching it }
        val ioClient = getIOClient(computer).getOrThrow()
        val osClient = IntervirtOSClient(
            IntervirtOSClient.Client(
                appEnv = appEnv,
                computer = computer,
                ioClient = ioClient,
                docker = getDockerManager(computer, ioClient).getOrThrow(),
            ),
        )
        osClient.init().getOrThrow()
        intervirtOSClients[computer.id] = osClient
        logger.debug { "Retrieved IntervirtOSClient of ${computer.id}" }
        osClient
    }

    suspend fun getDockerManager(
        computer: Device.Computer,
        ioClient: ContainerIOClient,
    ): Result<DockerManager> = runSuspendingCatching {
        computer.requireExists()
        logger.debug { "Initializing DockerManager for ${computer.id}" }
        dockerManagers[computer.id]?.let { return@runSuspendingCatching it }
        val sshClient = ioClient as? ContainerSshClient
        val dockerManager =
            ActualDockerManager(
                appEnv,
                dockerHostOverride ?: "ssh://127.0.0.1:${sshClient?.port ?: virtualContainerIOPort}",
            )
        dockerManagers[computer.id] = dockerManager
        logger.debug { "Initialized DockerManager for ${computer.id}" }
        dockerManager
    }

    private suspend fun clearUnusedNetworks(): Result<Unit> = guestManager.getNetworks().map { networks ->
        logger.debug { "Clearing unused networks" }
        networks
            .filter { it.value.isEmpty() }
            .forEach {
                guestManager.removeNetwork(it.key).getOrThrow()
            }
        logger.debug { "Cleared unused networks" }
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

    override suspend fun close() = runSuspendingCatching {
        logger.debug { "Closing DeviceManager" }
        intervirtOSClients.forEach { (_, client) -> client.close().getOrThrow() }
        dockerManagers.forEach { (_, manager) -> manager.close().getOrThrow() }
        containerIOClients.forEach { (_, client) -> client.close().getOrThrow() }
        logger.debug { "Closed DeviceManager" }
    }
}

fun getFreePort() = ServerSocket(0).use { it.localPort }
fun Int.isValidPort() = this in 1..65535

fun String.isValidProtocol() = this == "tcp" || this == "udp"

fun PortForwarding.validate(): Boolean {

    return externalPort.isValidPort() && internalPort.isValidPort() && protocol.isValidProtocol()
}
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
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.generateIpv4
import io.github.bommbomm34.intervirt.core.util.generateIpv6
import io.github.bommbomm34.intervirt.core.util.generateMac
import io.github.bommbomm34.intervirt.core.util.getLogger

import java.net.ServerSocket
import kotlin.random.Random


class DeviceManager(
    private val guestManager: GuestManager,
    private val qemuClient: QemuClient,
    private val executor: Executor,
    private val fileManager: FileManager,
    private val configuration: IntervirtConfiguration,
    private val appEnv: AppEnv,
) : AsyncCloseable {
    private val logger = appEnv.getLogger(DeviceManager::class)
    private val virtualContainerIO = appEnv.VIRTUAL_CONTAINER_IO
    private val virtualContainerIOPort = appEnv.VIRTUAL_CONTAINER_IO_PORT
    private val wipeVirtualOnClose = appEnv.WIPE_VIRTUAL_ON_CLOSE
    private val dockerHostOverride = appEnv.OVERRIDE_DOCKER_HOST.ifBlank { null }
    private val containerIOClients = mutableMapOf<String, ContainerIOClient>()
    private val dockerManagers = mutableMapOf<String, DockerManager>()
    private val intervirtOSClients = mutableMapOf<String, IntervirtOSClient>()

    suspend fun addComputer(name: String? = null, x: Int, y: Int, image: String): Result<Device.Computer> {
        val id = generateID("computer")
        val device = Device.Computer(
            id = id,
            image = image,
            name = name ?: id,
            x = x,
            y = y,
            ipv4 = configuration.generateIpv4(),
            ipv6 = configuration.generateIpv6(),
            mac = configuration.generateMac(),
            internetEnabled = false,
            portForwardings = mutableListOf(),
        )
        return addComputer(device)
    }

    suspend fun addComputer(device: Device.Computer): Result<Device.Computer> {
        logger.debug { "Adding device $device" }
        configuration.devices.add(device)
        return guestManager.addContainer(
            id = device.id,
            ipv4 = device.ipv4,
            ipv6 = device.ipv6,
            mac = device.mac,
            internet = false,
            image = device.image,
        ).map {
            logger.info { "Added device $device" }
            device
        }
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
        configuration.devices.add(device)
        logger.info { "Added device $device" }
        return device
    }

    suspend fun removeDevice(device: Device): Result<Unit> = runSuspendingCatching {
        logger.debug { "Removing device $device" }
        configuration.connections.removeIf { it.containsDevice(device) }
        configuration.devices.remove(device)
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
        logger.debug { "Connecting device $device1 to $device2" }
        val conn = device1 connect device2
        configuration.connections.add(conn)
        val networks = configuration.resolveNetworks(conn)
        guestManager.addNetworks(networks).getOrThrow()
        logger.info { "Connected device $device1 to $device2" }
    }

    suspend fun disconnectDevice(device1: Device, device2: Device): Result<Unit> = runSuspendingCatching {
        logger.debug { "Disconnecting device $device1 to $device2" }
        val conn = device1 connect device2
        configuration.connections.removeIf { it == conn }
        val networks = configuration.resolveNetworks(conn)
        networks.forEach { (name, network) ->
            network.forEach {
                guestManager.disconnect(it, name).getOrThrow()
            }
        }
        clearUnusedNetworks()
        logger.info { "Disconnected device $device1 to $device2" }
    }

    suspend fun setIpv4(device: Device.Computer, ipv4: String): Result<Unit> {
        logger.debug { "Setting $ipv4 of $device" }
        device.ipv4 = ipv4
        return guestManager.setIpv4(device.id, ipv4).map {
            logger.info { "Set $ipv4 of $device" }
        }
    }

    suspend fun setIpv6(device: Device.Computer, ipv6: String): Result<Unit> {
        logger.debug { "Setting $ipv6 of $device" }
        device.ipv6 = ipv6
        return guestManager.setIpv6(device.id, ipv6).map {
            logger.info { "Set $ipv6 of $device" }
        }
    }

    fun setName(device: Device, name: String) {
        device.name = name
        logger.debug { "Set name of ${device.name} to $name" }
    }

    suspend fun setInternetEnabled(device: Device.Computer, enabled: Boolean): Result<Unit> {
        logger.debug { "Setting internet enabled of ${device.id} to $enabled" }
        device.internetEnabled = enabled
        return guestManager.setInternetAccess(device.id, enabled).map {
            logger.info { "Set internet enabled of ${device.id} to $enabled" }
        }
    }

    suspend fun start(computer: Device.Computer): Result<Unit> {
        logger.debug { "Starting ${computer.id}" }
        return guestManager.startContainer(computer.id).map {
            logger.info { "Started ${computer.id}" }
        }
    }

    suspend fun stop(computer: Device.Computer): Result<Unit> {
        logger.debug { "Stopping ${computer.id}" }
        return guestManager.stopContainer(computer.id).map {
            logger.info { "Stopped ${computer.id}" }
        }
    }

    suspend fun addPortForwarding(
        device: Device.Computer,
        portForwarding: PortForwarding,
    ): Result<Unit> {
        logger.debug { "Add port forwarding $portForwarding for ${device.id}" }
        device.portForwardings.add(portForwarding)
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

    suspend fun removePortForwarding(externalPort: Int, protocol: String): Result<Unit> {
        logger.debug { "Remove port forwarding of $externalPort" }
        configuration.devices.forEach { device ->
            if (device is Device.Computer)
                device.portForwardings.removeIf { it.externalPort == externalPort }
        }
        if (!virtualContainerIO) qemuClient.removePortForwarding(
            protocol = protocol,
            externalPort = externalPort,
        ).onFailure { return Result.failure(it) }
        return guestManager.removePortForwarding(externalPort, protocol).map {
            logger.info { "Removed port forwarding of $externalPort" }
        }
    }

    suspend fun getIOClient(computer: Device.Computer): Result<ContainerIOClient> {
        logger.debug { "Retrieving IO client of ${computer.id}" }
        val cached = containerIOClients[computer.id]?.let { Result.success(it) }
        if (cached != null) return cached
        return if (virtualContainerIO) Result.success(initVirtualIOClient(computer)) else initSshClient(computer)
    }


    suspend fun initSshClient(computer: Device.Computer): Result<ContainerSshClient> {
        val port = getFreePort()
        logger.debug { "Initializing SSH client for ${computer.id} on port $port" }
        return addPortForwarding(
            device = computer,
            portForwarding = PortForwarding(
                protocol = "tcp",
                internalPort = 22,
                externalPort = port,
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
        logger.debug { "Initializing virtual container IO client for ${computer.id}" }
        val client = VirtualContainerIOClient(computer.id, wipeVirtualOnClose, executor, fileManager)
        containerIOClients[computer.id] = client
        logger.debug { "Initialized virtual container IO client for ${computer.id}" }
        return client
    }

    suspend fun getIntervirtOSClient(computer: Device.Computer) = runSuspendingCatching {
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
        logger.debug { "Initializing DockerManager for ${computer.id}" }
        dockerManagers[computer.id]?.let { return@runSuspendingCatching it }
        val sshClient = ioClient as? ContainerSshClient
        val dockerManager =
            ActualDockerManager(appEnv, dockerHostOverride ?: "ssh://127.0.0.1:${sshClient?.port ?: virtualContainerIOPort}")
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
            if (configuration.devices.all { it.id != id }) return id
        }
    }

    override suspend fun close() = runSuspendingCatching {
        logger.debug { "Closing DeviceManager" }
        intervirtOSClients.forEach { (_, client) -> client.close().getOrThrow() }
        dockerManagers.forEach { (_, manager) -> manager.close().getOrThrow() }
        containerIOClients.forEach { (_, client) -> client.close().getOrThrow() }
        logger.debug { "Closed DeviceManager" }
    }
}

fun getFreePort() = ServerSocket(0).use { it.localPort }
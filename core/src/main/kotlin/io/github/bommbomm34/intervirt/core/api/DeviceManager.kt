package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.impl.ContainerSshClient
import io.github.bommbomm34.intervirt.core.api.impl.VirtualContainerIOClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.ServerSocket
import kotlin.random.Random


class DeviceManager(
    private val guestManager: GuestManager,
    private val qemuClient: QemuClient,
    private val executor: Executor,
    private val fileManager: FileManager,
    private val configuration: IntervirtConfiguration,
    appEnv: AppEnv,
) : AsyncCloseable {
    private val logger = KotlinLogging.logger { }
    private val enableAgent = appEnv.ENABLE_AGENT
    private val virtualContainerIO = appEnv.VIRTUAL_CONTAINER_IO
    private val virtualContainerIOPort = appEnv.VIRTUAL_CONTAINER_IO_PORT
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
            ipv4 = generateIpv4(),
            ipv6 = generateIpv6(),
            mac = generateMac(),
            internetEnabled = false,
            portForwardings = mutableListOf(),
        )
        logger.debug { "Adding device $device" }
        configuration.devices.add(device)
        return if (enableAgent) {
            val res = guestManager.addContainer(device.id, device.ipv4, device.ipv6, device.mac, false, image)
            res.check(device)
        } else Result.success(device)
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
        if (device is Device.Computer && enableAgent) {
            guestManager.removeContainer(device.id).getOrThrow()
        }
    }

    suspend fun connectDevice(device1: Device, device2: Device): Result<Unit> {
        logger.debug { "Connecting device $device1 to $device2" }
        configuration.connections.add(configuration.connect(device1, device2))
        if (enableAgent) {
            val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
            device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
                device1ConnectedComputers.forEach { computer2 ->
                    val res = guestManager.connect(computer1.id, computer2.id)
                    res.onFailure { return res }
                }
            }
        }
        return Result.success(Unit)
    }

    suspend fun disconnectDevice(device1: Device, device2: Device): Result<Unit> {
        logger.debug { "Disconnecting device $device1 to $device2" }
        configuration.connections.removeIf { it == configuration.connect(device1, device2) }
        if (enableAgent) {
            val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
            device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
                device1ConnectedComputers.forEach { computer2 ->
                    val res = guestManager.disconnect(computer1.id, computer2.id)
                    res.onFailure { return res }
                }
            }
        }
        return Result.success(Unit)
    }

    suspend fun setIpv4(device: Device.Computer, ipv4: String): Result<Unit> {
        logger.debug { "Setting $ipv4 of $device" }
        device.ipv4 = ipv4
        return if (enableAgent) {
            val res = guestManager.setIpv4(device.id, ipv4)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun setIpv6(device: Device.Computer, ipv6: String): Result<Unit> {
        logger.debug { "Setting $ipv6 of $device" }
        device.ipv6 = ipv6
        return if (enableAgent) {
            val res = guestManager.setIpv6(device.id, ipv6)
            res.check(Unit)
        } else Result.success(Unit)
    }

    fun setName(device: Device, name: String) {
        device.name = name
    }

    suspend fun setInternetEnabled(device: Device.Computer, enabled: Boolean): Result<Unit> {
        logger.debug { "Set internet enabled of ${device.id} to $enabled" }
        device.internetEnabled = enabled
        return if (enableAgent) {
            val res = guestManager.setInternetAccess(device.id, enabled)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun start(computer: Device.Computer) = guestManager.startContainer(computer.id)

    suspend fun stop(computer: Device.Computer) = guestManager.stopContainer(computer.id)

    suspend fun addPortForwarding(
        device: Device.Computer,
        internalPort: Int,
        externalPort: Int,
        protocol: String,
    ): Result<Unit> {
        logger.debug { "Add port forwarding $internalPort:$externalPort for ${device.id}" }
        device.portForwardings.add(PortForwarding(protocol, externalPort, internalPort))
        qemuClient.addPortForwarding(
            protocol = protocol,
            hostPort = externalPort,
            guestPort = externalPort, // Guest is not the container itself
        ).onFailure { return Result.failure(it) }
        return if (enableAgent) {
            val res = guestManager.addPortForwarding(device.id, internalPort, externalPort, protocol)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun removePortForwarding(externalPort: Int, protocol: String): Result<Unit> {
        logger.debug { "Remove port forwarding of $externalPort" }
        configuration.devices.forEach { device ->
            if (device is Device.Computer)
                device.portForwardings.removeIf { it.hostPort == externalPort }
        }
        qemuClient.removePortForwarding(
            protocol = protocol,
            hostPort = externalPort,
        ).onFailure { return Result.failure(it) }
        return if (enableAgent) {
            val res = guestManager.removePortForwarding(externalPort, protocol)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun getIOClient(computer: Device.Computer): Result<ContainerIOClient> =
        containerIOClients[computer.id]?.let { Result.success(it) } ?: if (virtualContainerIO) Result.success(
            initVirtualIOClient(computer),
        ) else initSshClient(computer)

    suspend fun initSshClient(computer: Device.Computer): Result<ContainerSshClient> {
        val port = getFreePort()
        return addPortForwarding(
            device = computer,
            internalPort = 22,
            externalPort = port,
            protocol = "tcp",
        ).map {
            val sshClient = ContainerSshClient(port, this)
            containerIOClients[computer.id] = sshClient
            sshClient
        }
    }

    fun initVirtualIOClient(computer: Device.Computer): VirtualContainerIOClient {
        val client = VirtualContainerIOClient(computer.id, executor, fileManager, virtualContainerIOPort)
        containerIOClients[computer.id] = client
        return client
    }

    suspend fun getIntervirtOSClient(computer: Device.Computer) = runSuspendingCatching {
        val osClient = IntervirtOSClient(
            IntervirtOSClient.Client(
                computer = computer,
                ioClient = getIOClient(computer).getOrThrow(),
                docker = getDockerManager(computer).getOrThrow()
            ),
        )
        osClient.init().getOrThrow()
        intervirtOSClients[computer.id] = osClient
        osClient
    }

    suspend fun getDockerManager(computer: Device.Computer): Result<DockerManager> = runSuspendingCatching {
        dockerManagers[computer.id]?.let { return@runSuspendingCatching it }
        val port = if (!virtualContainerIO) {
            val freePort = getFreePort()
            addPortForwarding(
                device = computer,
                internalPort = 2375,
                externalPort = freePort,
                protocol = "tcp"
            ).getOrThrow()
            freePort
        } else 2375
        val dockerManager = DockerManager("tcp://127.0.0.1:$port", this)
        dockerManagers[computer.id] = dockerManager
        dockerManager
    }

    private fun generateID(prefix: String): String {
        while (true) {
            val id = prefix + "-" + Random.nextInt(999999)
            if (configuration.devices.all { it.id != id }) return id
        }
    }

    private fun generateMac(): String {
        val rand = { Random.nextInt(256).toString(16) }
        while (true) {
            val mac = "${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.mac != mac else true }) return mac
        }
    }

    private fun <T, R> Result<T>.check(ifSuccess: R): Result<R> {
        return exceptionOrNull()?.let { Result.failure(it) } ?: Result.success(ifSuccess)
    }

    fun generateIpv4(): String {
        val rand = { Random.nextInt(256) }
        while (true) {
            val ipv4 = "192.168.${rand()}.${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv4 != ipv4 else true }) return ipv4
        }
    }

    fun generateIpv6(): String {
        val rand = { Random.nextInt(65536).toString(16) }
        val randFirst = { Random.nextInt(256).toString(16) }
        while (true) {
            val ipv6 = "fd${randFirst()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv6 != ipv6 else true }) return ipv6
        }
    }

    override suspend fun close() = runSuspendingCatching {
        intervirtOSClients.forEach { (_, client) -> client.close().getOrThrow() }
        dockerManagers.forEach { (_, manager) -> manager.close().getOrThrow() }
        containerIOClients.forEach { (_, client) -> client.close().getOrThrow() }
    }
}

fun getFreePort() = ServerSocket(0).use { it.localPort }
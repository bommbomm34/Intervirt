package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.api.impl.ContainerSshClient
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Address
import io.github.bommbomm34.intervirt.data.AppEnv
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.PortForwarding
import io.github.bommbomm34.intervirt.data.connect
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.ServerSocket
import kotlin.random.Random


class DeviceManager(
    private val guestManager: GuestManager,
    private val qemuClient: QemuClient,
    private val virtualIOClient: Boolean,
    appEnv: AppEnv,
) {
    private val logger = KotlinLogging.logger { }
    private val enableAgent = appEnv.enableAgent
    private val containerIOClients = mutableMapOf<Device.Computer, ContainerIOClient>()

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
            portForwardings = mutableListOf()
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
            y = y
        )
        logger.debug { "Adding device $device" }
        configuration.devices.add(device)
        return device
    }

    suspend fun removeDevice(device: Device): Result<Unit> {
        logger.debug { "Removing device $device" }
        configuration.connections.removeIf { it.containsDevice(device) }
        configuration.devices.remove(device)
        containerIOClients[device]?.close()
        containerIOClients.remove(device)
        return if (device is Device.Computer && enableAgent) {
            val res = guestManager.removeContainer(device.id)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun connectDevice(device1: Device, device2: Device): Result<Unit> {
        logger.debug { "Connecting device $device1 to $device2" }
        configuration.connections.add(device1 connect device2)
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
        configuration.connections.removeIf { it == device1 connect device2 }
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

    suspend fun addPortForwarding(
        device: Device.Computer,
        internalPort: Int,
        externalPort: Int,
        protocol: String
    ): Result<Unit> {
        logger.debug { "Add port forwarding $internalPort:$externalPort for ${device.id}" }
        device.portForwardings.add(PortForwarding(protocol, externalPort, internalPort))
        qemuClient.addPortForwarding(
            protocol = "tcp", // TODO: It should be editable,
            hostPort = externalPort,
            guestPort = externalPort // Guest is not the container itself
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
            hostPort = externalPort
        ).onFailure { return Result.failure(it) }
        return if (enableAgent) {
            val res = guestManager.removePortForwarding(externalPort, protocol)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun getProxyUrl(computer: Device.Computer): Result<Address> {
        val port = getFreePort()
        return addPortForwarding(
            device = computer,
            internalPort = 1080,
            externalPort = port,
            protocol = "tcp"
        ).map { Address("127.0.0.1", port) }
    }

    suspend fun getIOClient(computer: Device.Computer): Result<ContainerIOClient> {
        // Check if there is an existing client
        containerIOClients[computer]?.let { return Result.success(it) }
        return if (virtualIOClient){
            val port = getFreePort()
            addPortForwarding(
                device = computer,
                internalPort = 22,
                externalPort = port,
                protocol = "tcp"
            ).map {
                val sshClient = ContainerSshClient(port)
                containerIOClients[computer] = sshClient
                sshClient
            }
        } else TODO("Use a mock client here")
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

    private fun getFreePort() = ServerSocket(0).use { it.localPort }

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

    fun close(){
        containerIOClients.forEach { (_, client) -> client.close() }
    }
}
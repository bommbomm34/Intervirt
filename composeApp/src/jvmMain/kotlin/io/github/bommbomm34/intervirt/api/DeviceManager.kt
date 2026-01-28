package io.github.bommbomm34.intervirt.api

import com.jediterm.terminal.TtyConnector
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.connect
import io.github.bommbomm34.intervirt.exceptions.ContainerExecutionException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import java.net.ServerSocket
import kotlin.random.Random

class DeviceManager(
    private val agentClient: AgentClient,
    private val qemuClient: QemuClient,
    preferences: Preferences,
) {
    private val logger = KotlinLogging.logger { }
    private val enableAgent = preferences.ENABLE_AGENT

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
            portForwardings = mutableMapOf()
        )
        logger.debug { "Adding device $device" }
        configuration.devices.add(device)
        return if (enableAgent) {
            val res = agentClient.addContainer(device.id, device.ipv4, device.ipv6, device.mac, false, image)
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
        return if (device is Device.Computer && enableAgent) {
            val res = agentClient.removeContainer(device.id)
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
                    val res = agentClient.connect(computer1.id, computer2.id)
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
                    val res = agentClient.disconnect(computer1.id, computer2.id)
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
            val res = agentClient.setIpv4(device.id, ipv4)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun setIpv6(device: Device.Computer, ipv6: String): Result<Unit> {
        logger.debug { "Setting $ipv6 of $device" }
        device.ipv6 = ipv6
        return if (enableAgent) {
            val res = agentClient.setIpv6(device.id, ipv6)
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
            val res = agentClient.setInternetAccess(device.id, enabled)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun runCommand(computer: Device.Computer, commands: List<String>): Flow<CommandStatus> = TODO("Not yet implemented")

    suspend fun addPortForwarding(device: Device.Computer, internalPort: Int, externalPort: Int, protocol: String): Result<Unit> {
        logger.debug { "Add port forwarding $internalPort:$externalPort for ${device.id}" }
        device.portForwardings[internalPort] = externalPort
        qemuClient.addPortForwarding(
            protocol = "tcp", // TODO: It should be editable,
            hostPort = externalPort,
            guestPort = externalPort // Guest is not the container itself
        ).onFailure { return Result.failure(it) }
        return if (enableAgent) {
            val res = agentClient.addPortForwarding(device.id, internalPort, externalPort, protocol)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun removePortForwarding(externalPort: Int, protocol: String): Result<Unit> {
        logger.debug { "Remove port forwarding of $externalPort" }
        configuration.devices.forEach { device ->
            if (device is Device.Computer)
                device.portForwardings.entries.removeIf { it.value == externalPort }
        }
        qemuClient.removePortForwarding(
            protocol = "tcp", // TODO: It should be editable
            hostPort = externalPort
        ).onFailure { return Result.failure(it) }
        return if (enableAgent) {
            val res = agentClient.removePortForwarding(externalPort, protocol)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun getProxy(computer: Device.Computer): Result<String> {
        val proxyPort = getFreePort()
        // Add port forwarding for proxy
        addPortForwarding(
            device = computer,
            externalPort = proxyPort,
            internalPort = 1080,
            protocol = "tcp"
        ).onFailure { return Result.failure(it) }
        // Start proxy
        val total = runCommand(computer, listOf("rc-service", "danted", "start")).getTotalCommandStatus()
        if (total.statusCode!! != 0) return Result.failure(ContainerExecutionException(total.message!!))
        return Result.success("127.0.0.1:$proxyPort")
    }

    suspend fun getTtyConnector(computer: Device.Computer): TtyConnector {
        TODO("Not yet implemented")
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
}
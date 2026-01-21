package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.data.connect
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlin.random.Random

object DeviceManager {
    val logger = KotlinLogging.logger { }

    suspend fun addComputer(name: String? = null, x: Int, y: Int, image: String): Result<Device.Computer> {
        val id = generateID("computer")
        val device = Device.Computer(
            id = id,
            image = image,
            name = name ?: id,
            x = x,
            y = y,
            ipv4 = generateIPv4(),
            ipv6 = generateIPv6(),
            internetEnabled = false,
            portForwardings = mutableMapOf()
        )
        logger.debug { "Adding device $device" }
        configuration.devices.add(device)
        val res = AgentClient.addContainer(device.id, device.ipv4, device.ipv6, false, image)
        return res.check(device)
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
        configuration.devices.remove(device)
        configuration.connections.removeIf { it.containsDevice(device) }
        return if (device is Device.Computer) {
            val res = AgentClient.removeContainer(device.id)
            res.check(Unit)
        } else Result.success(Unit)
    }

    suspend fun connectDevice(device1: Device, device2: Device): Result<Unit> {
        logger.debug { "Connecting device $device1 to $device2" }
        configuration.connections.add(device1 connect device2)
        val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
        device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
            device1ConnectedComputers.forEach { computer2 ->
                val res = AgentClient.connect(computer1.id, computer2.id)
                res.onFailure { return res }
            }
        }
        return Result.success(Unit)
    }

    suspend fun disconnectDevice(device1: Device, device2: Device): Result<Unit> {
        logger.debug { "Disconnecting device $device1 to $device2" }
        configuration.connections.removeIf { it == device1 connect device2 }
        val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
        device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
            device1ConnectedComputers.forEach { computer2 ->
                val res = AgentClient.disconnect(computer1.id, computer2.id)
                res.onFailure { return res }
            }
        }
        return Result.success(Unit)
    }

    suspend fun setIPv4(device: Device.Computer, ipv4: String): Result<Unit> {
        logger.debug { "Setting $ipv4 of $device" }
        device.ipv4 = ipv4
        val res = AgentClient.setIPv4(device.id, ipv4)
        return res.check(Unit)
    }

    suspend fun setIPv6(device: Device.Computer, ipv6: String): Result<Unit> {
        logger.debug { "Setting $ipv6 of $device" }
        device.ipv6 = ipv6
        val res = AgentClient.setIPv6(device.id, ipv6)
        return res.check(Unit)
    }

    suspend fun exportComputer(computer: Device.Computer): Result<File> {
        logger.debug { "Exporting $computer" }
        return AgentClient.getDisk(computer.id)
    }
    fun runCommand(computer: Device.Computer, command: String) = AgentClient.runCommand(computer.id, command)

    fun setName(device: Device, name: String) {
        device.name = name
    }

    suspend fun setInternetEnabled(device: Device.Computer, enabled: Boolean): Result<Unit> {
        logger.debug { "Set internet enabled of ${device.id} to $enabled" }
        device.internetEnabled = enabled
        val res = AgentClient.setInternetAccess(device.id, enabled)
        return res.check(Unit)
    }

    suspend fun addPortForwarding(device: Device.Computer, internalPort: Int, externalPort: Int): Result<Unit> {
        logger.debug { "Add port forwarding $internalPort:$externalPort for ${device.id}" }
        device.portForwardings[internalPort] = externalPort
        val res = AgentClient.addPortForwarding(device.id, internalPort, externalPort)
        return res.check(Unit)
    }

    suspend fun removePortForwarding(externalPort: Int): Result<Unit> {
        logger.debug { "Remove port forwarding of $externalPort" }
        configuration.devices.forEach { device ->
            if (device is Device.Computer)
                device.portForwardings.entries.removeIf { it.value == externalPort }
        }
        val res = AgentClient.removePortForwarding(externalPort)
        return res.check(Unit)
    }

    private fun generateID(prefix: String): String {
        while (true) {
            val id = prefix + "-" + Random.nextInt(999999)
            if (configuration.devices.all { it.id != id }) return id
        }
    }

    private fun <T, R> Result<T>.check(ifSuccess: R): Result<R> {
        return exceptionOrNull()?.let { Result.failure(it) } ?: Result.success(ifSuccess)
    }

    fun generateIPv4(): String {
        val rand = { Random.nextInt(256) }
        while (true) {
            val ipv4 = "192.168.${rand()}.${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv4 != ipv4 else true }) return ipv4
        }
    }

    fun generateIPv6(): String {
        val rand = { Random.nextInt(65536).toString(16) }
        val randFirst = { Random.nextInt(256).toString(16) }
        while (true) {
            val ipv6 = "fd${randFirst()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv6 != ipv6 else true }) return ipv6
        }
    }
}
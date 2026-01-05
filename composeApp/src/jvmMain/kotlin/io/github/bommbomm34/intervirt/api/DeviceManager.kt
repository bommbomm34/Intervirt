package io.github.bommbomm34.intervirt.api

import androidx.compose.ui.geometry.Offset
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.IncusImage
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.data.connect
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlin.random.Random

object DeviceManager {
    suspend fun addComputer(name: String, x: Float, y: Float, image: IncusImage): Device {
        val device = Device.Computer(
            id = generateID("computer"),
            image = image.fullName(),
            name = name,
            x = x,
            y = y,
            ipv4 = generateIPv4(),
            ipv6 = generateIPv6(),
            internetEnabled = false,
            portForwardings = mutableMapOf()
        )
        logger.debug { "Adding device $device" }
        configuration = configuration.copy(devices = configuration.devices + device)
        AgentClient.addContainer(device.id, device.ipv4, device.ipv6, false, image.fullName())
        return device
    }

    fun addSwitch(name: String, x: Float, y: Float): Device {
        val device = Device.Switch(
            id = generateID("switch"),
            name = name,
            x = x,
            y = y
        )
        logger.debug { "Adding device $device" }
        configuration = configuration.copy(devices = configuration.devices + device)
        return device
    }

    suspend fun removeDevice(device: Device) {
        logger.debug { "Removing device $device" }
        configuration = configuration.copy(devices = configuration.devices - device)
        configuration = configuration.copy(connections = configuration.connections - configuration.connections.filter {
            it.containsDevice(device)
        }
            .toSet())
        AgentClient.removeContainer(device.id)
    }

    suspend fun connectDevice(device1: Device, device2: Device) {
        logger.debug { "Connecting device $device1 to $device2" }
        configuration = configuration.copy(connections = configuration.connections + (device1 connect device2))
        val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
        device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
            device1ConnectedComputers.forEach { computer2 -> AgentClient.connect(computer1.id, computer2.id) }
        }
    }

    suspend fun disconnectDevice(device1: Device, device2: Device) {
        logger.debug { "Disconnecting device $device1 to $device2" }
        configuration = configuration.copy(connections = configuration.connections - (device1 connect device2))
        val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
        device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
            device1ConnectedComputers.forEach { computer2 -> AgentClient.disconnect(computer1.id, computer2.id) }
        }

    }

    suspend fun setIPv4(device: Device.Computer, ipv4: String) {
        logger.debug { "Setting $ipv4 of $device" }
        configuration = configuration.copy(devices = configuration.devices.map { if (it == device) device.copy(ipv4 = ipv4) else it })
        AgentClient.setIPv4(device.id, ipv4)
    }

    suspend fun setIPv6(device: Device.Computer, ipv6: String) {
        logger.debug { "Setting $ipv6 of $device" }
        configuration = configuration.copy(devices = configuration.devices.map { if (it == device && it is Device.Computer) it.copy(ipv6 = ipv6) else it })
        AgentClient.setIPv6(device.id, ipv6)
    }

    fun exportComputer(computer: Device.Computer): Flow<ResultProgress<File>> = flow {
        logger.debug { "Exporting disk of $computer" }
        emit(ResultProgress.result(AgentClient.getDisk(computer.id)))
    }

    fun runCommand(computer: Device.Computer, command: String) = AgentClient.runCommand(computer.id, command)

    fun setPosition(device: Device, offset: Offset){
        configuration = configuration.copy(devices = configuration.devices.map {
            if (it == device){
                when (it) {
                    is Device.Computer -> it.copy(x = offset.x, y = offset.y)
                    is Device.Switch -> it.copy(x = offset.x, y = offset.y)
                }
            } else it
        })
    }

    fun setName(device: Device, name: String){
        configuration = configuration.copy(devices = configuration.devices.map {
            if (it == device){
                when (it) {
                    is Device.Computer -> it.copy(name = name)
                    is Device.Switch -> it.copy(name = name)
                }
            } else it
        })
    }

    fun setImage(device: Device.Computer, image: IncusImage){
        configuration = configuration.copy(devices = configuration.devices.map { if (it == device && it is Device.Computer) it.copy(image = image.fullName()) else it })
    }

    suspend fun setInternetEnabled(device: Device.Computer, enabled: Boolean){
        logger.debug { "Set internet enabled of ${device.id} to $enabled" }
        configuration = configuration.copy(devices = configuration.devices.map { if (it == device && it is Device.Computer) it.copy(internetEnabled = enabled) else it })
        AgentClient.setInternetAccess(device.id, enabled)
    }

    suspend fun addPortForwarding(device: Device.Computer, internalPort: Int, externalPort: Int){
        logger.debug { "Add port forwarding $internalPort:$externalPort for ${device.id}" }
        configuration = configuration.copy(devices = configuration.devices.map { if (it == device && it is Device.Computer) it.copy(portForwardings = it.portForwardings + (internalPort to externalPort)) else it })
        AgentClient.addPortForwarding(device.id, internalPort, externalPort)
    }

    suspend fun removePortForwarding(externalPort: Int){
        logger.debug { "Remove port forwarding of $externalPort" }
        configuration = configuration.copy(devices = configuration.devices.map { if (it is Device.Computer && it.portForwardings.values.contains(externalPort)) it.copy(portForwardings = it.portForwardings.filter { it.value != externalPort }) else it })
        AgentClient.removePortForwarding(externalPort)
    }

    private fun generateID(prefix: String): String {
        while (true) {
            val id = prefix + "-" + Random.nextInt(999999)
            if (configuration.devices.all { it.id != id }) return id
        }
    }

    private fun generateIPv4(): String {
        val rand = { Random.nextInt(256) }
        while (true) {
            val ipv4 = "192.168.${rand()}.${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv4 == ipv4 else true }) return ipv4
        }
    }

    private fun generateIPv6(): String {
        val rand = { Random.nextInt(65536).toString(16) }
        val randFirst = { Random.nextInt(256).toString(16) }
        while (true) {
            val ipv6 = "fd${randFirst()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv6 == ipv6 else true }) return ipv6
        }
    }
}
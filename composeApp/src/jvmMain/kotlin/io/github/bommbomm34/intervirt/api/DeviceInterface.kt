package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.IncusImage
import io.github.bommbomm34.intervirt.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.connect
import kotlin.random.Random

class DeviceInterface (val configuration: IntervirtConfiguration, val agent: AgentInterface) {
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
        configuration.devices.add(device)
        agent.addContainer(device.id, device.ipv4, device.ipv6, false)
        return device
    }

    fun addSwitch(name: String, x: Float, y: Float): Device {
        val device = Device.Switch(
            id = generateID("switch"),
            name = name,
            x = x,
            y = y
        )
        configuration.devices.add(device)
        return device
    }

    suspend fun removeDevice(device: Device){
        configuration.devices.remove(device)
        configuration.connections.removeIf { it.containsDevice(device) }
        agent.removeContainer(device.id)
    }

    suspend fun connectDevice(device1: Device, device2: Device){
        configuration.connections.add(device1 connect device2)
        val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
        device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
            device1ConnectedComputers.forEach { computer2 -> agent.connect(computer1.id, computer2.id) }
        }
    }

    suspend fun disconnectDevice(device1: Device, device2: Device){
        configuration.connections.remove(device1 connect device2)
        val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
        device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
            device1ConnectedComputers.forEach { computer2 -> agent.disconnect(computer1.id, computer2.id) }
        }
        
    }

    suspend fun changeIPv4(device: Device.Computer, ipv4: String){
        device.ipv4 = ipv4
        agent.setIPv4(device.id, ipv4)
    }

    suspend fun changeIPv6(device: Device.Computer, ipv6: String){
        device.ipv6 = ipv6
        agent.setIPv4(device.id, ipv6)
    }

    private fun generateID(prefix: String): String {
        while (true) {
            val id = prefix + Random.nextInt(999999)
            if (configuration.devices.all { it.id != id }) return id
        }
    }

    fun generateIPv4(): String {
        val rand = { Random.nextInt(256) }
        while (true){
            val ipv4 = "192.168.${rand()}.${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv4 == ipv4 else true }) return ipv4
        }
    }

    fun generateIPv6(): String {
        val rand = { Random.nextInt(65536).toString(16) }
        val randFirst = { Random.nextInt(256).toString(16) }
        while (true){
            val ipv6 = "fd${randFirst()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv6 == ipv6 else true }) return ipv6
        }
    }
}
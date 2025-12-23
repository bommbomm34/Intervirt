package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.FileManagement
import io.github.bommbomm34.intervirt.data.IncusImage
import kotlin.collections.mutableListOf
import kotlin.random.Random

class DeviceInterface (val configuration: IntervirtConfiguration, val fileManagement: FileManagement) {
    fun addComputer(name: String, x: Float, y: Float, image: IncusImage): Device {
        val device = Device.Computer(
            id = generateID("computer"),
            image = image.fullName(),
            name = name,
            x = x,
            y = y,
            ipv4 = generateIPv4(),
            ipv6 = generateIPv6(),
            internetEnabled = false,
            portForwardings = mutableMapOf(),
            connected = mutableListOf()
        )
        configuration.devices.add(device)
        return device
    }

    fun addSwitch(name: String, x: Float, y: Float): Device {
        val device = Device.Switch(
            id = generateID("switch"),
            name = name,
            x = x,
            y = y,
            connected = mutableListOf()
        )
        configuration.devices.add(device)
        return device
    }

    fun removeDevice(device: Device){
        configuration.devices.remove(device)
        configuration.devices.forEach { it.connected.remove(device.id) }
    }

    fun connectDevice(device1: Device, device2: Device){
        device1.connected.add(device2.id)
        device2.connected.add(device1.id)
    }

    fun disconnectDevice(device1: Device, device2: Device){
        device1.connected.remove(device2.id)
        device2.connected.remove(device1.id)
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
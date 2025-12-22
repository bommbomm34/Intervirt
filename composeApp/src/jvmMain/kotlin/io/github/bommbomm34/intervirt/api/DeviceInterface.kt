package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.Configuration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.FileManagement
import kotlin.random.Random

class DeviceInterface (val configuration: Configuration, val fileManagement: FileManagement) {
    fun addComputer(name: String, x: Float, y: Float): Device {
        val device = Device(
            id = generateID("computer"),
            name = name,
            x = x,
            y = y,
            connected = mutableListOf()
        )
        configuration.devices.add(device)
        return device
    }

    fun addSwitch(name: String, x: Float, y: Float): Device {
        val device = Device(
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
}
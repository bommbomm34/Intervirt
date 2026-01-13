package io.github.bommbomm34.intervirt.data.stateful

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Device

sealed class ViewDevice(val device: Device) {
    val id = device.id
    var name by mutableStateOf(device.name)
    var x by mutableStateOf(device.x)
    var y by mutableStateOf(device.y)

    data class Computer(val computer: Device.Computer) : ViewDevice(computer) {
        val image by mutableStateOf(computer.image)
        var ipv4 by mutableStateOf(computer.ipv4)
        var ipv6 by mutableStateOf(computer.ipv6)
        var internetEnabled by mutableStateOf(computer.internetEnabled)
        val portForwardings =
            mutableStateMapOf<Int, Int>().apply { putAll(computer.portForwardings) } // internalPort:externalPort

        override fun toDevice() = configuration.devices.first { this.id == it.id } as Device.Computer
    }

    data class Switch(val switch: Device.Switch) : ViewDevice(switch) {
        override fun toDevice() = configuration.devices.first { this.id == it.id } as Device.Switch
    }

    open fun toDevice() = configuration.devices.first { this.id == it.id }
}

fun Device.toViewDevice() = when (this) {
    is Device.Switch -> ViewDevice.Switch(this)
    is Device.Computer -> ViewDevice.Computer(this)
}
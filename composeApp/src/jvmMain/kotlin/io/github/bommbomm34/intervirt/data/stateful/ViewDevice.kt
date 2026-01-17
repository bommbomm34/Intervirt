package io.github.bommbomm34.intervirt.data.stateful

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.DevicesPc
import compose.icons.tablericons.Switch
import intervirt.composeapp.generated.resources.Res
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.statefulConf
import org.jetbrains.compose.resources.vectorResource

sealed class ViewDevice(val device: Device) {
    val id = device.id
    var name by mutableStateOf(device.name)
    var x by mutableStateOf(device.x)
    var y by mutableStateOf(device.y)
    val offset
        get() = Offset(x.toFloat(), y.toFloat())

    data class Computer(val computer: Device.Computer) : ViewDevice(computer) {
        val image by mutableStateOf(computer.image)
        var ipv4 by mutableStateOf(computer.ipv4)
        var ipv6 by mutableStateOf(computer.ipv6)
        var internetEnabled by mutableStateOf(computer.internetEnabled)
        val portForwardings =
            mutableStateMapOf<Int, Int>().apply { putAll(computer.portForwardings) } // internalPort:externalPort

        override fun toDevice() = configuration.devices.first { this.id == it.id } as Device.Computer
        override fun getVector() = TablerIcons.DevicesPc
    }

    data class Switch(val switch: Device.Switch) : ViewDevice(switch) {
        override fun toDevice() = configuration.devices.first { this.id == it.id } as Device.Switch
        override fun getVector() = TablerIcons.Switch
    }

    open fun toDevice() = configuration.devices.first { this.id == it.id }

    infix fun connect(other: ViewDevice) = ViewConnection(this, other)
    abstract fun getVector(): ImageVector
}

fun Device.toViewDevice() = when (this) {
    is Device.Switch -> ViewDevice.Switch(this)
    is Device.Computer -> ViewDevice.Computer(this)
}

fun Device.toExistingViewDevice() = statefulConf.devices.first { id == it.id }
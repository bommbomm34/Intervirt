package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.DevicesPc
import compose.icons.tablericons.Switch
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.core.data.PortForwarding

sealed class ViewDevice {
    abstract val device: Device
    abstract val id: String
    abstract var name: String
    abstract var x: Int
    abstract var y: Int
    val offset
        get() = Offset(x.toFloat(), y.toFloat())

    data class Computer(override val device: Device.Computer) : ViewDevice() {
        override val id = device.id
        override var name by mutableStateOf(device.name)
        override var x by mutableStateOf(device.x)
        override var y by mutableStateOf(device.y)
        val image by mutableStateOf(device.image)
        var ipv4 by mutableStateOf(device.ipv4)
        var ipv6 by mutableStateOf(device.ipv6)
        val mac = device.mac
        var internetEnabled by mutableStateOf(device.internetEnabled)
        val portForwardings =
            mutableListOf<PortForwarding>().apply { addAll(device.portForwardings) } // internalPort:externalPort

        override fun getVector() = TablerIcons.DevicesPc
        override fun canConnect(configuration: IntervirtConfiguration) = configuration.connections.count { it.containsDevice(device) } == 0
    }

    data class Switch(override val device: Device.Switch,
    ) : ViewDevice() {
        override val id = device.id
        override var name by mutableStateOf(device.name)
        override var x by mutableStateOf(device.x)
        override var y by mutableStateOf(device.y)
        override fun getVector() = TablerIcons.Switch
        override fun canConnect(configuration: IntervirtConfiguration) = true
    }
    infix fun connect(other: ViewDevice) = ViewConnection(this, other)
    abstract fun getVector(): ImageVector
    abstract fun canConnect(configuration: IntervirtConfiguration): Boolean
}

fun Device.toViewDevice() = when (this) {
    is Device.Switch -> ViewDevice.Switch(this)
    is Device.Computer -> ViewDevice.Computer(this)
}
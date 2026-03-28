/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Hub
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.Project
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
        override var name by mutableStateOf(device.name.get())
        override var x by mutableStateOf(device.x.get())
        override var y by mutableStateOf(device.y.get())
        val image by mutableStateOf(device.image)
        var ipv4 by mutableStateOf(device.ipv4.get())
        var ipv6 by mutableStateOf(device.ipv6.get())
        var running by mutableStateOf(false) // Not inherited by Device.Computer
        val mac = device.mac.get()
        var internetEnabled by mutableStateOf(device.internetEnabled.get())
        val portForwardings =
            mutableStateListOf<PortForwarding>().apply { addAll(device.portForwardings.value) }

        override fun getVector() = Icons.Default.Computer
        override fun canConnect(project: Project) =
            project.connections.count { it.containsDevice(device) } == 0
    }

    data class Switch(
        override val device: Device.Switch,
    ) : ViewDevice() {
        override val id = device.id
        override var name by mutableStateOf(device.name.get())
        override var x by mutableStateOf(device.x.get())
        override var y by mutableStateOf(device.y.get())
        override fun getVector() = Icons.Default.Hub // Switches aren't hubs!
        override fun canConnect(project: Project) = true
    }

    infix fun connect(other: ViewDevice) = ViewConnection(this, other)
    abstract fun getVector(): ImageVector
    abstract fun canConnect(project: Project): Boolean
}

fun Device.toViewDevice() = when (this) {
    is Device.Switch -> ViewDevice.Switch(this)
    is Device.Computer -> ViewDevice.Computer(this)
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import io.github.bommbomm34.intervirt.core.data.Device.Computer
import kotlinx.serialization.Serializable

@Serializable
sealed class Device {

    abstract val id: String
    abstract var name: String
    abstract var x: Int
    abstract var y: Int

    @Serializable
    data class Computer(
        override val id: String,
        val image: String,
        override var name: String,
        override var x: Int,
        override var y: Int,
        var ipv4: String,
        var ipv6: String,
        val mac: String,
        var internetEnabled: Boolean,
        val portForwardings: MutableList<PortForwarding>, // internalPort:externalPort
    ) : Device()

    @Serializable
    data class Switch(
        override val id: String,
        override var name: String,
        override var x: Int,
        override var y: Int,
    ) : Device()

    override fun equals(other: Any?): Boolean {
        return other is Device && other.id == id
    }

    override fun hashCode(): Int = id.hashCode()
}

fun String.toDevice(configuration: IntervirtConfiguration) = configuration.devices.first { it.id == this }

fun IntervirtConfiguration.getConnectedComputers(
    device: Device,
    exceptDevices: Set<Device> = emptySet(),
): List<Computer> {
    val connected = getConnectedDevices(device)
    val connectedComputers = mutableSetOf<Computer>() // Usage of a set is important because duplicates can occur
    connected.filter { device -> exceptDevices.all { device.id != it.id } }
        .forEach {
            if (it is Computer) connectedComputers.add(it) else
                connectedComputers.addAll(getConnectedComputers(it, exceptDevices + device))
        }
    return connectedComputers.toList()
}

fun IntervirtConfiguration.getConnectedDevices(device: Device): List<Device> {
    return connections.mapNotNull {
        val (device1, device2) = it.getDevices(this)
        if (device1 == device) device2 else if (device2 == device) device1 else null
    }
}
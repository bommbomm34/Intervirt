/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import arrow.optics.optics
import io.github.bommbomm34.intervirt.core.data.Device.Computer
import kotlinx.serialization.Serializable

@Serializable
@optics
sealed class Device {

    abstract val id: String
    abstract val name: String
    abstract val x: Int
    abstract val y: Int

    @Serializable
    @optics
    data class Computer(
        override val id: String,
        val image: String,
        override val name: String,
        override val x: Int,
        override val y: Int,
        val ipv4: String,
        val ipv6: String,
        val mac: String,
        val internetEnabled: Boolean,
        val portForwardings: List<PortForwarding>,
    ) : Device() {
        companion object
    }

    @Serializable
    data class Switch(
        override val id: String,
        override val name: String,
        override val x: Int,
        override val y: Int,
    ) : Device()

    override fun equals(other: Any?): Boolean {
        return other is Device && other.id == id
    }

    override fun hashCode(): Int = id.hashCode()

    companion object
}

fun String.toDevice(devices: List<Device>) = devices.first { it.id == this@toDevice }

fun getConnectedComputers(
    device: Device,
    devices: List<Device>,
    connections: List<DeviceConnection>,
    exceptDevices: Set<Device> = emptySet(),
): List<Computer> {
    val connected = getConnectedDevices(device, devices, connections)
    val connectedComputers = mutableSetOf<Computer>() // Usage of a set is important because duplicates can occur
    connected.filter { device -> exceptDevices.all { device.id != it.id } }
        .forEach {
            if (it is Computer) connectedComputers.add(it) else
                connectedComputers.addAll(getConnectedComputers(it, devices, connections, exceptDevices + device))
        }
    return connectedComputers.toList()
}

fun getConnectedDevices(
    device: Device,
    devices: List<Device>,
    connections: List<DeviceConnection>,
): List<Device> {
    return connections.mapNotNull {
        val (device1, device2) = it.getDevices(devices)
        if (device1 == device) device2 else if (device2 == device) device1 else null
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Device> Project.getDevice(device: T): T = devices.first { it.id == device.id } as T

@Suppress("UNCHECKED_CAST")
fun <T : Device> Project.getDeviceOrNull(device: T): T? = devices.firstOrNull { it.id == device.id } as? T

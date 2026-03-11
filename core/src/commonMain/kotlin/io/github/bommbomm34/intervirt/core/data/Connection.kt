/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import kotlinx.serialization.Serializable

/**
 * A connection
 *
 * This class represents a logical connection between two devices.
 * Intervirt Agent will only receive connections that are *computer to computer*
 *
 * @param id1 first device id
 * @property id2 second device id
 * @constructor Creates a connection
 */
@Serializable
sealed class DeviceConnection {
    abstract val id1: String
    abstract val id2: String

    /**
     * Connection between two switches
     */
    @Serializable
    data class Switch(
        override val id1: String,
        override val id2: String,
    ) : DeviceConnection()

    /**
     * Connection between two computers
     */
    @Serializable
    data class Computer(
        override val id1: String,
        override val id2: String,
    ) : DeviceConnection() {
        @Suppress("UNCHECKED_CAST")
        override fun getDevices(devices: List<Device>): Pair<Device.Computer, Device.Computer> {
            return Pair(id1.toDevice(devices), id2.toDevice(devices)) as Pair<Device.Computer, Device.Computer>
        }
    }

    /**
     * Connection between a switch and a computer
     */
    @Serializable
    data class SwitchComputer(
        override val id1: String, // Switch
        override val id2: String, // Computer
    ) : DeviceConnection() {
        @Suppress("UNCHECKED_CAST")
        override fun getDevices(devices: List<Device>): Pair<Device.Switch, Device.Computer> {
            return Pair(id1.toDevice(devices), id2.toDevice(devices)) as Pair<Device.Switch, Device.Computer>
        }
    }

    /**
     * Checks if device is in the connection
     * @param device device to check
     * @return `true` if device is in the connection and `false` otherwise
     */
    fun containsDevice(device: Device) = containsID(device.id)
    fun containsID(id: String) = id1 == id || id2 == id

    override fun equals(other: Any?): Boolean {
        return other is DeviceConnection && ((id1 == other.id1 && id2 == other.id2) ||
                (id1 == other.id2 && id2 == other.id1))
    }

    override fun hashCode(): Int {
        val (firstDevice, secondDevice) = if (id1.hashCode() > id2.hashCode()) id1 to id2 else id2 to id1
        var result = firstDevice.hashCode()
        result = 31 * result + secondDevice.hashCode()
        return result
    }

    open fun getDevices(devices: List<Device>): Pair<Device, Device> =
        Pair(id1.toDevice(devices), id2.toDevice(devices))
}

infix fun Device.connect(other: Device) = when (this) {
    is Device.Computer if other is Device.Computer -> DeviceConnection.Computer(id, other.id)
    is Device.Switch if other is Device.Switch -> DeviceConnection.Switch(id, other.id)
    is Device.Switch if other is Device.Computer -> DeviceConnection.SwitchComputer(id, other.id)
    is Device.Computer if other is Device.Switch -> DeviceConnection.SwitchComputer(other.id, id)
    else -> error("Invalid connection of $this and $other")
}
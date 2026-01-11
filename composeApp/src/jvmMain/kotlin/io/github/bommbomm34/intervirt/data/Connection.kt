package io.github.bommbomm34.intervirt.data

import io.ktor.network.sockets.Connection
import kotlinx.serialization.Serializable

/**
 * A connection
 *
 * This class represents a logical connection between two devices.
 * Intervirt Agent will only receive connections that are *computer to computer*
 *
 * @param device1 first device
 * @property device2 second device
 * @constructor Creates a connection
 */
@Serializable
sealed class DeviceConnection (
    val device1: Device,
    val device2: Device
) {
    /**
     * Connection between two switches
     */
    data class Switch(
        val switch1: Device.Switch,
        val switch2: Device.Switch
    ): DeviceConnection(switch1, switch2)

    /**
     * Connection between two computers
     */
    data class Computer(
        val computer1: Device.Computer,
        val computer2: Device.Computer
    ): DeviceConnection(computer1, computer2)

    /**
     * Connection between a switch and a computer
     */
    data class SwitchComputer(
        val switch: Device.Switch,
        val computer: Device.Computer
    ): DeviceConnection(switch, computer)

    /**
     * Checks if device is in the connection
     * @param device device to check
     * @return ```true``` if device is in the connection and ```false``` otherwise
     */
    fun containsDevice(device: Device) = device1 == device || device2 == device

    override fun equals(other: Any?): Boolean {
        return other is DeviceConnection && ((device1 == other.device1 && device2 == other.device2) ||
                (device1 == other.device2 && device2 == other.device1))
    }

    override fun hashCode(): Int {
        val (firstDevice, secondDevice) = if (device1.hashCode() > device2.hashCode()) device1 to device2 else device2 to device1
        var result = firstDevice.hashCode()
        result = 31 * result + secondDevice.hashCode()
        return result
    }


}

/**
 * @param otherDevice the device which should be connected with this device
 * @return a ```DeviceConnection``` based on the types of the given devices
 */
infix fun Device.connect(otherDevice: Device) = when (this) {
    is Device.Computer if otherDevice is Device.Computer -> DeviceConnection.Computer(this, otherDevice)
    is Device.Switch if otherDevice is Device.Switch -> DeviceConnection.Switch(this, otherDevice)
    is Device.Switch if otherDevice is Device.Computer -> DeviceConnection.SwitchComputer(this, otherDevice)
    is Device.Computer if otherDevice is Device.Switch -> DeviceConnection.SwitchComputer(otherDevice, this)
    else -> error("Invalid connection of $this and $otherDevice")
}
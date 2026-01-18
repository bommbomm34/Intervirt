package io.github.bommbomm34.intervirt.data

import io.ktor.network.sockets.Connection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
sealed class DeviceConnection {
    abstract val id1: String
    abstract val id2: String
    val device1: Device
        get() = id1.toDevice()
    val device2: Device
        get() = id2.toDevice()
    /**
     * Connection between two switches
     */
    @Serializable
    data class Switch(
        override val id1: String,
        override val id2: String
    ): DeviceConnection() {
        val switch1
            get() = id1.toDevice() as Device.Switch
        val switch2
            get() = id2.toDevice() as Device.Switch
    }

    /**
     * Connection between two computers
     */
    @Serializable
    data class Computer(
        override val id1: String,
        override val id2: String
    ): DeviceConnection() {
        val computer1
            get() = id1.toDevice() as Device.Computer
        val computer2
            get() = id2.toDevice() as Device.Computer
    }

    /**
     * Connection between a switch and a computer
     */
    @Serializable
    data class SwitchComputer(
        override val id1: String, // Switch
        override val id2: String // Computer
    ): DeviceConnection() {
        val switch
            get() = id1.toDevice() as Device.Switch
        val computer
            get () = id2.toDevice() as Device.Computer
    }

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
    is Device.Computer if otherDevice is Device.Computer -> DeviceConnection.Computer(this.id, otherDevice.id)
    is Device.Switch if otherDevice is Device.Switch -> DeviceConnection.Switch(this.id, otherDevice.id)
    is Device.Switch if otherDevice is Device.Computer -> DeviceConnection.SwitchComputer(this.id, otherDevice.id)
    is Device.Computer if otherDevice is Device.Switch -> DeviceConnection.SwitchComputer(otherDevice.id, this.id)
    else -> error("Invalid connection of $this and $otherDevice")
}
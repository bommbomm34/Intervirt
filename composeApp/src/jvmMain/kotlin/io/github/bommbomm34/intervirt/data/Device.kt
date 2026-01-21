package io.github.bommbomm34.intervirt.data

import androidx.compose.ui.geometry.Offset
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import kotlinx.serialization.SerialName
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
        val portForwardings: MutableMap<Int, Int> // internalPort:externalPort
    ) : Device()

    @Serializable
    data class Switch(
        override val id: String,
        override var name: String,
        override var x: Int,
        override var y: Int
    ) : Device()

    fun getConnectedDevices(totalConnections: List<DeviceConnection>) =
        totalConnections.mapNotNull { if (it.device1 == this) it.device2 else if (it.device2 == this) it.device1 else null }

    fun getConnectedComputers(totalConnections: List<DeviceConnection>, exceptDevice: Device? = null): List<Computer> {
        val connected = getConnectedDevices(totalConnections)
        val connectedComputers = mutableSetOf<Computer>() // Usage of a set is important because duplicates can occur
        connected.filter { device -> exceptDevice?.let { device != exceptDevice } ?: true }
            .forEach { if (it is Computer) connectedComputers.add(it) else 
            connectedComputers.addAll(it.getConnectedComputers(totalConnections, this)) }
        return connectedComputers.toList()
    }

    override fun equals(other: Any?): Boolean {
        return other is Device && other.id == id
    }

    override fun hashCode(): Int = id.hashCode()
}

fun String.toDevice() = configuration.devices.first { it.id == this }
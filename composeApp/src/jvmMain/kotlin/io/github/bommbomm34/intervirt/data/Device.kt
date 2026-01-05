package io.github.bommbomm34.intervirt.data

import kotlinx.serialization.Serializable

@Serializable
sealed class Device(
    open val id: String,
    open val name: String,
    open var x: Int,
    open var y: Int
) {
    data class Computer(
        override val id: String,
        val image: String,
        override val name: String,
        override var x: Int,
        override var y: Int,
        val ipv4: String,
        val ipv6: String,
        val internetEnabled: Boolean,
        val portForwardings: Map<Int, Int> // internalPort:externalPort
    ) : Device(id, name, x, y)

    data class Switch(
        override val id: String,
        override val name: String,
        override var x: Int,
        override var y: Int
    ) : Device(id, name, x, y)

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
}
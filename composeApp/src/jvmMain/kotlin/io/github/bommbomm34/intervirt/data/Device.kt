package io.github.bommbomm34.intervirt.data

import kotlinx.serialization.Serializable

@Serializable
sealed class Device(
    open val id: String,
    open var name: String,
    open var x: Float,
    open var y: Float,
    open val connected: MutableList<String> // List of IDs
){
    data class Computer(
        override val id: String,
        val image: String,
        override var name: String,
        override var x: Float,
        override var y: Float,
        var ipv4: String,
        var ipv6: String,
        var internetEnabled: Boolean,
        val portForwardings: MutableMap<Int, Int>, // internalPort:externalPort
        override val connected: MutableList<String>
    ) : Device(id, name, x, y, connected)

    data class Switch(
        override val id: String,
        override var name: String,
        override var x: Float,
        override var y: Float,
        override val connected: MutableList<String>
    ) : Device(id, name, x, y, connected)
}

fun Collection<Device>.getById(id: String) = first { it.id == id }
package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.bommbomm34.intervirt.core.data.DeviceConnection
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration

// Stateful IntervirtConfiguration for the UI
data class ViewConfiguration(
    private val intervirtConfiguration: IntervirtConfiguration,
) {
    var version by mutableStateOf(intervirtConfiguration.version)
    var author by mutableStateOf(intervirtConfiguration.author)
    val devices =
        mutableStateListOf<ViewDevice>().apply { intervirtConfiguration.devices.forEach { add(it.toViewDevice()) } }
    val connections =
        mutableStateListOf<ViewConnection>().apply { intervirtConfiguration.connections.forEach { add(it.toViewConnection()) } }

    fun update(configuration: ViewConfiguration) {
        author = configuration.author
        devices.clear()
        devices.addAll(configuration.devices)
        connections.clear()
        connections.addAll(configuration.connections)
    }

    fun exists(device: ViewDevice) = devices.any { it.id == device.id }

    private fun DeviceConnection.toViewConnection(): ViewConnection {
        val viewDevice1 = devices.first { it.id == device1.id }
        val viewDevice2 = devices.first { it.id == device2.id }
        return ViewConnection(viewDevice1, viewDevice2)
    }
}
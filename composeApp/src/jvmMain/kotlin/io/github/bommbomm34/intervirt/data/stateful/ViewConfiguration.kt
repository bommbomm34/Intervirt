package io.github.bommbomm34.intervirt.data.stateful

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.bommbomm34.intervirt.data.DeviceConnection
import io.github.bommbomm34.intervirt.data.IntervirtConfiguration

// Stateful IntervirtConfiguration for the UI
data class ViewConfiguration(
    private val intervirtConfiguration: IntervirtConfiguration
) {
    var version by mutableStateOf(intervirtConfiguration.version)
    var author by mutableStateOf(intervirtConfiguration.author)
    val devices =
        mutableStateListOf<ViewDevice>().apply { intervirtConfiguration.devices.forEach { add(it.toViewDevice()) } }
    val connections = mutableStateListOf<DeviceConnection>().apply { addAll(intervirtConfiguration.connections) }
}
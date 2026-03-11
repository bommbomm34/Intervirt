/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.DeviceConnection
import io.github.bommbomm34.intervirt.core.data.Project

// Stateful Project for the UI
class ViewProject(
    version: String,
    author: String,
    devices: MutableList<Device>,
    connections: MutableList<DeviceConnection>,
) {
    var version by mutableStateOf(version)
    var author by mutableStateOf(author)
    val devices =
        mutableStateListOf<ViewDevice>().apply { devices.forEach { add(it.toViewDevice()) } }
    val connections =
        mutableStateListOf<ViewConnection>().apply { connections.forEach { add(it.toViewConnection()) } }

    fun update(project: ViewProject) {
        author = project.author
        devices.clear()
        devices.addAll(project.devices)
        connections.clear()
        connections.addAll(project.connections)
    }

    fun exists(device: ViewDevice) = devices.any { it.id == device.id }

    private fun DeviceConnection.toViewConnection(): ViewConnection {
        val viewDevice1 = devices.first { it.id == id1 }
        val viewDevice2 = devices.first { it.id == id2 }
        return ViewConnection(viewDevice1, viewDevice2)
    }
}

suspend fun Project.toViewProject(): ViewProject {
    devices.withLockLet { devices ->
        connections.withLockLet { connections ->
            return ViewProject(
                version = version,
                author = author.get(),
                devices = devices,
                connections = connections,
            )
        }
    }
}

fun Project.toViewProjectUnsafe(): ViewProject {
    return ViewProject(
        version = version,
        author = author.get(),
        devices = devices.value,
        connections = connections.value,
    )
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import arrow.core.raise.context.bind
import arrow.core.raise.either
import arrow.optics.optics
import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.addNetworkIfNotExists
import io.github.bommbomm34.intervirt.core.data.agent.Network
import io.github.bommbomm34.intervirt.core.exceptions.DeprecatedException
import io.github.bommbomm34.intervirt.core.util.ext.lastResult
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import jdk.jfr.internal.OldObjectSample.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

/**
 * Intervirt project
 */
@Serializable
@optics
data class Project(
    val version: String = CURRENT_VERSION,
    val author: String = "",
    val devices: List<Device> = listOf(),
    val connections: List<DeviceConnection> = listOf(),
) {
    companion object
}

fun Project.resolveNetworks(vararg connections: DeviceConnection): Map<String, Network> {
    val networks = mutableMapOf<String, Network>()
    val connections = connections.ifEmpty { this.connections.toTypedArray() }
    // Add switch networks
    devices.forEach { device ->
        if (device is Device.Switch && connections.any { it.containsDevice(device) }) {
            val name = networkNameOfSwitch(device.id)
            networks[name] = getConnectedComputers(device, devices, this.connections).map { it.id }
        }
    }
    // Add computer networks
    connections.forEach { conn ->
        if (conn is DeviceConnection.Computer) {
            val name = networkNameOfComputers(conn.id1, conn.id2)
            networks[name] = listOf(conn.id1, conn.id2)
        }
    }
    return networks
}

suspend fun GuestManager.addNetworks(networks: Map<String, Network>): AppResult<Unit> = either {
    networks.forEach { (name, network) ->
        addNetworkIfNotExists(name).bind()
        network.forEach {
            connect(it, name).bind()
        }
    }
}

fun GuestManager.syncProject(project: Project): Flow<ResultProgress<Unit>> = flow {
    either {
        val version = getInfo().bind().version
        if (version != CURRENT_VERSION) {
            emit(ResultProgress.failure(Failure.VersionMismatch(version)))
        } else {
            emit(
                ResultProgress.proceed(
                    percentage = 0f,
                    message = "Starting synchronisation...",
                ),
            )
            emit(
                ResultProgress.proceed(
                    percentage = 0f,
                    message = "Wiping old data...",
                ),
            )
            wipe().collect { emit(it.clone(percentage = it.percentage * 0.2f)) }
            emit(
                ResultProgress.proceed(
                    percentage = 0.2f,
                    message = "Creating devices...",
                ),
            )
            project.devices.forEachIndexed { i, device ->
                if (device is Device.Computer) {
                    val progress = 0.2f + (i.toFloat() / project.devices.size) * 0.6f
                    emit(
                        ResultProgress.proceed(
                            percentage = progress,
                            message = "Creating device ${device.name} with id ${device.id}",
                        ),
                    )
                    addContainer(
                        id = device.id,
                        ipv4 = device.ipv4,
                        ipv6 = device.ipv6,
                        mac = device.mac,
                        internet = device.internetEnabled,
                        image = device.image,
                    ).lastResult().bind() // TODO: Propagate progress
                    device.portForwardings.forEach { portForwarding ->
                        emit(
                            ResultProgress.proceed(
                                percentage = progress,
                                message = "Adding port forwarding for ${device.name}: ${portForwarding.protocol}:${portForwarding.internalPort}:${portForwarding.externalPort}",
                            ),
                        )
                        addPortForwarding(
                            device.id,
                            portForwarding.internalPort,
                            portForwarding.externalPort,
                            portForwarding.protocol,
                        ).bind()
                    }
                }
            }
            emit(
                ResultProgress.proceed(
                    percentage = 0.8f,
                    message = "Connecting devices...",
                ),
            )
            addNetworks(project.resolveNetworks()).bind()
            emit(
                ResultProgress.proceed(
                    percentage = 1f,
                    message = "Synchronisation successfully completed",
                ),
            )
        }
    }.onLeft { emit(ResultProgress.failure(it)) }
}

fun networkNameOfComputers(id1: String, id2: String) = "$id1-$id2-network"
fun networkNameOfSwitch(id: String) = "$id-network"

fun <T : Device> Project.modifyDevice(
    device: T,
    action: (T) -> T,
): Project = Project.devices.modify(this) { devices ->
    devices.map { if (it.id == device.id) action(device) else it }
}

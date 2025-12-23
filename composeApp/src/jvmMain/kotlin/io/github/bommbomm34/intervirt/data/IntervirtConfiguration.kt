package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.api.AgentInterface
import io.github.bommbomm34.intervirt.exceptions.DeprecatedException
import io.github.bommbomm34.intervirt.result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

// IntervirtConfiguration of an Intervirt project
@Serializable
data class IntervirtConfiguration(
    val version: String,
    val author: String,
    val devices: MutableList<Device>
) {
    fun syncConfiguration(agent: AgentInterface): Flow<Result<String>> = flow {
        // Check version
        if (agent.getVersion() != CURRENT_VERSION)
            emit(DeprecatedException().result())
        else {
            emit("Starting synchronisation".result())
            emit("Wiping old data".result())
            agent.wipe()
            emit("Creating devices".result())
            devices.forEach { device ->
                if (device is Device.Computer) {
                    emit("Creating device ${device.name} with id ${device.id}".result())
                    agent.addContainer(
                        id = device.id,
                        initialIPv4 = device.ipv4,
                        initialIPv6 = device.ipv6,
                        internet = device.internetEnabled
                    )
                    device.portForwardings.forEach { portForwarding ->
                        emit("Adding port forwarding ${portForwarding.key}:${portForwarding.value} to ${device.name}".result())
                        agent.forwardPort(device.id, portForwarding.key, portForwarding.value)
                    }
                }
            }
            emit("Connecting devices".result())
            devices.forEach { device ->
                device.connected.forEach { peerId ->
                    emit("Conencting device ${device.name} to ${devices.getById(peerId).name}".result())
                    agent.connect(device.id, peerId)
                }
            }
            emit("Synchronisation successfully completed".result())
            emit("END".result())
        }
    }
}
package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.api.AgentClient
import io.github.bommbomm34.intervirt.exceptions.DeprecatedException
import io.github.bommbomm34.intervirt.result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

// Configuration of an Intervirt project
@Serializable
data class IntervirtConfiguration(
    val version: String,
    var author: String,
    val devices: MutableList<Device>,
    val connections: MutableList<DeviceConnection>
) {
    fun syncConfiguration(agent: AgentClient): Flow<Result<String>> = flow {
        // Check version
        if (agent.getVersion() != CURRENT_VERSION) {
            emit(DeprecatedException().result())
        } else {
            emit("Starting synchronisation".result())
            emit("Wiping old data".result())
            agent.wipe().collect { emit((it.message ?: "").result()) }
            emit("Creating devices".result())
            devices.forEach { device ->
                if (device is Device.Computer) {
                    emit("Creating device ${device.name} with id ${device.id}".result())
                    agent.addContainer(
                        id = device.id,
                        initialIPv4 = device.ipv4,
                        initialIPv6 = device.ipv6,
                        internet = device.internetEnabled,
                        image = device.image
                    )
                    device.portForwardings.forEach { portForwarding ->
                        emit("Adding port forwarding ${portForwarding.key}:${portForwarding.value} to ${device.name}".result())
                        agent.addPortForwarding(device.id, portForwarding.key, portForwarding.value)
                    }
                }
            }
            emit("Connecting devices".result())

            connections.forEach { conn ->
                emit("Connecting device ${conn.device1.name} to ${conn.device2.name}".result())
                when (conn) {
                    is DeviceConnection.Computer -> agent.connect(conn.device1.id, conn.device2.id)
                    is DeviceConnection.Switch -> {
                        val switch1ConnectedComputers = conn.switch1.getConnectedComputers(connections)
                        conn.switch2.getConnectedComputers(connections).forEach { computer1 ->
                            switch1ConnectedComputers.forEach { computer2 -> agent.connect(computer1.id, computer2.id) }
                        }
                    }
                    is DeviceConnection.SwitchComputer -> conn.switch.getConnectedComputers(connections).forEach { agent.connect(it.id, conn.computer.id) }
                }
            }
            emit("Synchronisation successfully completed".result())
            emit("END".result())
        }
    }
}
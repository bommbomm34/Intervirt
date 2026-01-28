package io.github.bommbomm34.intervirt.data

import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.api.AgentClient
import io.github.bommbomm34.intervirt.exceptions.DeprecatedException
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import java.io.File

// Configuration of an Intervirt project
@Serializable
data class IntervirtConfiguration(
    val version: String,
    var author: String,
    val devices: MutableList<Device>,
    val connections: MutableList<DeviceConnection>
) {
    fun syncConfiguration(agentClient: AgentClient): Flow<ResultProgress<Unit>> = flow {
        agentClient.getVersion()
            .onSuccess { version ->
                if (version != CURRENT_VERSION) {
                    emit(ResultProgress.failure(DeprecatedException()))
                } else {
                    emit(
                        ResultProgress.proceed(
                            percentage = 0f,
                            message = getString(Res.string.starting_synchronisation)
                        )
                    )
                    emit(
                        ResultProgress.proceed(
                            percentage = 0f,
                            message = getString(Res.string.wiping_old_data)
                        )
                    )
                    agentClient.wipe().collect { emit(it.copy(percentage = it.percentage * 0.2f)) }
                    emit(
                        ResultProgress.proceed(
                            percentage = 0.2f,
                            message = getString(Res.string.creating_devices)
                        )
                    )
                    devices.forEachIndexed { i, device ->
                        if (device is Device.Computer) {
                            val progress = 0.2f + (i.toFloat() / devices.size) * 0.6f
                            emit(
                                ResultProgress.proceed(
                                    percentage = progress,
                                    message = getString(Res.string.creating_device, device.name, device.id)
                                )
                            )
                            agentClient.addContainer(
                                id = device.id,
                                initialIpv4 = device.ipv4,
                                initialIpv6 = device.ipv6,
                                mac = device.mac,
                                internet = device.internetEnabled,
                                image = device.image
                            )
                            device.portForwardings.forEach { portForwarding ->
                                emit(
                                    ResultProgress.proceed(
                                        percentage = progress,
                                        message = getString(
                                            Res.string.adding_port_forwarding,
                                            "${portForwarding.key}:${portForwarding.value}",
                                            device.name
                                        )
                                    )
                                )
                                // TODO: Port forwardings should have protocol support for UDP
                                agentClient.addPortForwarding(device.id, portForwarding.key, portForwarding.value, "tcp")
                            }
                        }
                    }
                    emit(
                        ResultProgress.proceed(
                            percentage = 0.8f,
                            message = getString(Res.string.connecting_devices)
                        )
                    )

                    connections.forEachIndexed { i, conn ->
                        emit(
                            ResultProgress.proceed(
                                percentage = 0.8f + (i.toFloat() / connections.size) * 0.2f,
                                message = getString(Res.string.connecting_device, conn.device1.name, conn.device2.name)
                            )
                        )
                        when (conn) {
                            is DeviceConnection.Computer -> agentClient.connect(conn.device1.id, conn.device2.id)
                            is DeviceConnection.Switch -> {
                                val switch1ConnectedComputers = conn.switch1.getConnectedComputers(connections)
                                conn.switch2.getConnectedComputers(connections).forEach { computer1 ->
                                    switch1ConnectedComputers.forEach { computer2 ->
                                        agentClient.connect(
                                            computer1.id,
                                            computer2.id
                                        )
                                    }
                                }
                            }

                            is DeviceConnection.SwitchComputer -> conn.switch.getConnectedComputers(connections)
                                .forEach { agentClient.connect(it.id, conn.computer.id) }
                        }
                    }
                    emit(
                        ResultProgress.proceed(
                            percentage = 1f,
                            message = getString(Res.string.synchronisation_successfully_completed)
                        )
                    )
                }
            }
            .onFailure {
                emit(ResultProgress.failure(it))
            }
    }

    fun update(configuration: IntervirtConfiguration){
        author = configuration.author
        devices.clear()
        devices.addAll(configuration.devices)
        connections.clear()
        connections.addAll(configuration.connections)
    }
}
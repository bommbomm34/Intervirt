package io.github.bommbomm34.intervirt.core.data

import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.exceptions.DeprecatedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

// Configuration of an Intervirt project
@Serializable
data class IntervirtConfiguration(
    val version: String = CURRENT_VERSION,
    var author: String = "",
    val devices: MutableList<Device> = mutableListOf(),
    val connections: MutableList<DeviceConnection> = mutableListOf(),
) {
    companion object {
        fun default() = IntervirtConfiguration(
            version = CURRENT_VERSION,
            author = "",
            devices = mutableListOf(),
            connections = mutableListOf(),
        )
    }

    fun update(configuration: IntervirtConfiguration) {
        author = configuration.author
        devices.clear()
        devices.addAll(configuration.devices)
        connections.clear()
        connections.addAll(configuration.connections)
    }
}

fun GuestManager.syncConfiguration(conf: IntervirtConfiguration): Flow<ResultProgress<Unit>> = flow {
    getVersion()
        .onSuccess { version ->
            if (version != CURRENT_VERSION) {
                emit(ResultProgress.failure(DeprecatedException()))
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
                conf.devices.forEachIndexed { i, device ->
                    if (device is Device.Computer) {
                        val progress = 0.2f + (i.toFloat() / conf.devices.size) * 0.6f
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
                        )
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
                            )
                        }
                    }
                }
                emit(
                    ResultProgress.proceed(
                        percentage = 0.8f,
                        message = "Connecting devices...",
                    ),
                )

                conf.connections.forEachIndexed { i, conn ->
                    emit(
                        ResultProgress.proceed(
                            percentage = 0.8f + (i.toFloat() / conf.connections.size) * 0.2f,
                            message = "Connecting device ${conn.id1} with ${conn.id2}",
                        ),
                    )
                    when (conn) {
                        is DeviceConnection.Computer -> connect(conn.id1, conn.id2)
                        is DeviceConnection.Switch -> {
                            val (switch1, switch2) = conn.getDevices(conf)
                            val switch1ConnectedComputers = conf.getConnectedComputers(switch1)
                            conf.getConnectedComputers(switch2).forEach { computer1 ->
                                switch1ConnectedComputers.forEach { computer2 ->
                                    connect(
                                        computer1.id,
                                        computer2.id,
                                    )
                                }
                            }
                        }

                        is DeviceConnection.SwitchComputer -> {
                            val (switch, computer) = conn.getDevices(conf)
                            conf.getConnectedComputers(switch).forEach { connect(it.id, computer.id) }
                        }
                    }
                }
                emit(
                    ResultProgress.proceed(
                        percentage = 1f,
                        message = "Synchronisation successfully completed",
                    ),
                )
            }
        }
        .onFailure {
            emit(ResultProgress.failure(it))
        }
}
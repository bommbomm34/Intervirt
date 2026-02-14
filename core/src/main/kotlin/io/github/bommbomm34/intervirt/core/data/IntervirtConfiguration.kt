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
    val version: String,
    var author: String,
    val devices: MutableList<Device>,
    val connections: MutableList<DeviceConnection>
) {
    fun syncConfiguration(guestManager: GuestManager): Flow<ResultProgress<Unit>> = flow {
        guestManager.getVersion()
            .onSuccess { version ->
                if (version != CURRENT_VERSION) {
                    emit(ResultProgress.failure(DeprecatedException()))
                } else {
                    emit(
                        ResultProgress.proceed(
                            percentage = 0f,
                            message = "Starting synchronisation..."
                        )
                    )
                    emit(
                        ResultProgress.proceed(
                            percentage = 0f,
                            message = "Wiping old data..."
                        )
                    )
                    guestManager.wipe().collect { emit(it.clone(percentage = it.percentage * 0.2f)) }
                    emit(
                        ResultProgress.proceed(
                            percentage = 0.2f,
                            message = "Creating devices..."
                        )
                    )
                    devices.forEachIndexed { i, device ->
                        if (device is Device.Computer) {
                            val progress = 0.2f + (i.toFloat() / devices.size) * 0.6f
                            emit(
                                ResultProgress.proceed(
                                    percentage = progress,
                                    message = "Creating device ${device.name} with id ${device.id}"
                                )
                            )
                            guestManager.addContainer(
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
                                        message = "Adding port forwarding for ${device.name}: ${portForwarding.protocol}:${portForwarding.guestPort}:${portForwarding.hostPort}"
                                    )
                                )
                                guestManager.addPortForwarding(device.id, portForwarding.guestPort, portForwarding.hostPort, portForwarding.protocol)
                            }
                        }
                    }
                    emit(
                        ResultProgress.proceed(
                            percentage = 0.8f,
                            message = "Connecting devices..."
                        )
                    )

                    connections.forEachIndexed { i, conn ->
                        emit(
                            ResultProgress.proceed(
                                percentage = 0.8f + (i.toFloat() / connections.size) * 0.2f,
                                message = "Connecting device ${conn.device1.name} with ${conn.device2.name}"
                            )
                        )
                        when (conn) {
                            is DeviceConnection.Computer -> guestManager.connect(conn.device1.id, conn.device2.id)
                            is DeviceConnection.Switch -> {
                                val switch1ConnectedComputers = conn.switch1.getConnectedComputers(connections)
                                conn.switch2.getConnectedComputers(connections).forEach { computer1 ->
                                    switch1ConnectedComputers.forEach { computer2 ->
                                        guestManager.connect(
                                            computer1.id,
                                            computer2.id
                                        )
                                    }
                                }
                            }

                            is DeviceConnection.SwitchComputer -> conn.switch.getConnectedComputers(connections)
                                .forEach { guestManager.connect(it.id, conn.computer.id) }
                        }
                    }
                    emit(
                        ResultProgress.proceed(
                            percentage = 1f,
                            message = "Synchronisation successfully completed"
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
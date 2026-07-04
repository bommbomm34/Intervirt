/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import io.github.bommbomm34.intervirt.core.data.AgentInfo
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.ContainerInfo
import io.github.bommbomm34.intervirt.core.data.agent.Network
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import kotlinx.coroutines.flow.Flow

interface GuestManager : AsyncCloseable {
    context(_: Raise<Failure>)
    suspend fun addContainer(
        id: String,
        ipv4: String,
        ipv6: String,
        mac: String,
        internet: Boolean,
        image: String,
    ): Flow<ResultProgress<Unit>>

    context(_: Raise<Failure>)
    suspend fun addContainer(container: ContainerInfo) = addContainer(
        id = container.id,
        ipv4 = container.ipv4,
        ipv6 = container.ipv6,
        mac = container.mac,
        internet = container.internet,
        image = container.image,
    )

    context(_: Raise<Failure>)
    suspend fun removeContainer(id: String)

    context(_: Raise<Failure>)
    suspend fun setIpv4(id: String, newIP: String)

    context(_: Raise<Failure>)
    suspend fun setIpv6(id: String, newIP: String)

    context(_: Raise<Failure>)
    suspend fun connect(container: String, network: String)

    context(_: Raise<Failure>)
    suspend fun disconnect(container: String, network: String)

    context(_: Raise<Failure>)
    suspend fun setInternetAccess(id: String, enabled: Boolean)

    context(_: Raise<Failure>)
    suspend fun addPortForwarding(id: String, internalPort: Int, externalPort: Int, protocol: String)

    context(_: Raise<Failure>)
    suspend fun removePortForwarding(id: String, externalPort: Int, protocol: String)

    context(_: Raise<Failure>)
    suspend fun startContainer(id: String)

    context(_: Raise<Failure>)
    suspend fun stopContainer(id: String)

    context(_: Raise<Failure>)
    fun wipe(): Flow<ResultProgress<Unit>>

    context(_: Raise<Failure>)
    fun update(): Flow<ResultProgress<Unit>>

    context(_: Raise<Failure>)
    suspend fun getInfo(): AgentInfo

    context(_: Raise<Failure>)
    suspend fun getContainers(): List<ContainerInfo>

    context(_: Raise<Failure>)
    suspend fun addNetwork(name: String)

    context(_: Raise<Failure>)
    suspend fun removeNetwork(name: String)

    context(_: Raise<Failure>)
    suspend fun getNetworks(): Map<String, Network>
}

context(_: Raise<Failure>)
suspend fun GuestManager.addNetworkIfNotExists(name: String) {
    recover(
        block = { addNetwork(name) },
        recover = { failure ->
            if (failure is Failure.OperationAlreadyPerformed) Unit else raise(failure)
        }
    )
}

/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.ContainerInfo
import io.github.bommbomm34.intervirt.core.data.agent.Network
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.ext.recoverAlreadyPerformed
import kotlinx.coroutines.flow.Flow

interface GuestManager : AsyncCloseable {
    suspend fun addContainer(
        id: String,
        ipv4: String,
        ipv6: String,
        mac: String,
        internet: Boolean,
        image: String,
    ): Flow<ResultProgress<Unit>>

    suspend fun addContainer(container: ContainerInfo) = addContainer(
        id = container.id,
        ipv4 = container.ipv4,
        ipv6 = container.ipv6,
        mac = container.mac,
        internet = container.internet,
        image = container.image,
    )

    suspend fun removeContainer(id: String): AppResult<Unit>

    suspend fun setIpv4(id: String, newIP: String): AppResult<Unit>

    suspend fun setIpv6(id: String, newIP: String): AppResult<Unit>

    suspend fun connect(container: String, network: String): AppResult<Unit>

    suspend fun disconnect(container: String, network: String): AppResult<Unit>

    suspend fun setInternetAccess(id: String, enabled: Boolean): AppResult<Unit>

    suspend fun addPortForwarding(id: String, internalPort: Int, externalPort: Int, protocol: String): AppResult<Unit>

    suspend fun removePortForwarding(id: String, externalPort: Int, protocol: String): AppResult<Unit>

    suspend fun startContainer(id: String): AppResult<Unit>

    suspend fun stopContainer(id: String): AppResult<Unit>

    fun wipe(): Flow<ResultProgress<Unit>>

    fun update(): Flow<ResultProgress<Unit>>

    suspend fun shutdown(): AppResult<Unit>

    suspend fun reboot(): AppResult<Unit>

    suspend fun getVersion(): AppResult<String>

    suspend fun getContainers(): AppResult<List<ContainerInfo>>

    suspend fun addNetwork(name: String): AppResult<Unit>

    suspend fun removeNetwork(name: String): AppResult<Unit>

    suspend fun getNetworks(): AppResult<Map<String, Network>>
}

suspend fun GuestManager.addNetworkIfNotExists(name: String): AppResult<Unit> = addNetwork(name).recoverAlreadyPerformed()

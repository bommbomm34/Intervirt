/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

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
    ): Result<Unit>

    suspend fun addContainer(container: ContainerInfo) = addContainer(
        id = container.id,
        ipv4 = container.ipv4,
        ipv6 = container.ipv6,
        mac = container.mac,
        internet = container.internet,
        image = container.image,
    )

    suspend fun removeContainer(id: String): Result<Unit>

    suspend fun setIpv4(id: String, newIP: String): Result<Unit>

    suspend fun setIpv6(id: String, newIP: String): Result<Unit>

    suspend fun connect(container: String, network: String): Result<Unit>

    suspend fun disconnect(container: String, network: String): Result<Unit>

    suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit>

    suspend fun addPortForwarding(id: String, internalPort: Int, externalPort: Int, protocol: String): Result<Unit>

    suspend fun removePortForwarding(externalPort: Int, protocol: String): Result<Unit>

    suspend fun startContainer(id: String): Result<Unit>

    suspend fun stopContainer(id: String): Result<Unit>

    fun wipe(): Flow<ResultProgress<Unit>>

    fun update(): Flow<ResultProgress<Unit>>

    suspend fun shutdown(): Result<Unit>

    suspend fun reboot(): Result<Unit>

    suspend fun getVersion(): Result<String>

    suspend fun getContainers(): Result<List<ContainerInfo>>

    suspend fun addNetwork(name: String): Result<Unit>

    suspend fun removeNetwork(name: String): Result<Unit>

    suspend fun getNetworks(): Result<Map<String, Network>>
}

suspend fun GuestManager.addNetworkIfNotExists(name: String): Result<Unit> = addNetwork(name).recoverAlreadyPerformed()
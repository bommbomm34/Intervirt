/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.SshGuestClient
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.ContainerInfo
import io.github.bommbomm34.intervirt.core.data.agent.Network
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.util.ext.runSuspendingCatching
import kotlinx.coroutines.flow.Flow

/**
 * [GuestManager] using Unix Sockets for communication
 *
 * Requires a [SshGuestClient] which needs to be initialized before.
 */
class SshGuestManager(
    private val client: SshGuestClient,
) : GuestManager {
    init {
        require(client.isInitialized) { "Given SshGuestClient isn't initialized" }
    }

    override suspend fun addContainer(
        id: String,
        ipv4: String,
        ipv6: String,
        mac: String,
        internet: Boolean,
        image: String,
    ): Result<Unit> = runSuspendingCatching {
        // Create container
        incus("launch", image, id).getOrThrow()
        // Create network device
        createNetwork(id, ipv4, ipv6, mac, internet).getOrThrow()
    }

    override suspend fun removeContainer(id: String): Result<Unit> = incus("delete", id)

    override suspend fun setIpv4(id: String, newIP: String): Result<Unit> = incus(
        "config", "device", "set",
        id,
        "eth0",
        "ipv4.address", newIP,
    )

    override suspend fun setIpv6(id: String, newIP: String): Result<Unit> = incus(
        "config", "device", "set",
        id,
        "eth0",
        "ipv6.address", newIP,
    )

    override suspend fun connect(container: String, network: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(container: String, network: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun removePortForwarding(
        externalPort: Int,
        protocol: String,
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun startContainer(id: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun stopContainer(id: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun wipe(): Flow<ResultProgress<Unit>> {
        TODO("Not yet implemented")
    }

    override fun update(): Flow<ResultProgress<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun shutdown(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun reboot(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getVersion(): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getContainers(): Result<List<ContainerInfo>> {
        TODO("Not yet implemented")
    }

    override suspend fun addNetwork(name: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun removeNetwork(name: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getNetworks(): Result<Map<String, Network>> {
        TODO("Not yet implemented")
    }

    override suspend fun close(): Result<Unit> = Result.success(Unit) // Closing SshGuestClient is handled separately

    // TODO: Review
    private suspend fun createNetwork(
        name: String,
        ipv4: String,
        ipv6: String,
        mac: String,
        enabled: Boolean,
    ): Result<Unit> {
        val nicCmd = if (enabled) arrayOf("nictype=bridged", "parent=eth0") else arrayOf("nictype=none")
        return incus(
            "config", "device", "add",
            name,
            "eth0",
            "nic",
            "hwaddr=$mac",
            "ipv4.address=$ipv4",
            "ipv6.address=$ipv6",
            *nicCmd,
        )
    }

    private suspend fun incus(vararg args: String): Result<Unit> = client.runCommand("incus", *args).mapCatching {
        it.getCommandResult()
            .asResult()
            .getOrThrow()
    }
}
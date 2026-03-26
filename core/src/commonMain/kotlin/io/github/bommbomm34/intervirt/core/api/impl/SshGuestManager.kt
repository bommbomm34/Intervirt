/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.SshGuestClient
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.ContainerInfo
import io.github.bommbomm34.intervirt.core.data.agent.Network
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.data.incus.IncusInstanceInfo
import io.github.bommbomm34.intervirt.core.data.incus.IncusNetwork
import io.github.bommbomm34.intervirt.core.data.incus.IncusNetworkForwardInfo
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.util.ext.flowCatching
import io.github.bommbomm34.intervirt.core.util.ext.runSuspendingCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * [GuestManager] using Unix Sockets for communication
 *
 * Requires a [SshGuestClient] which needs to be initialized before.
 */
class SshGuestManager(
    private val client: SshGuestClient,
) : GuestManager {
    init {
        check(client.isInitialized) { "Given SshGuestClient isn't initialized" }
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

    override suspend fun connect(container: String, network: String): Result<Unit> = incus(
        "network", "attach",
        network,
        container,
    )

    override suspend fun disconnect(container: String, network: String): Result<Unit> = incus(
        "network", "detach",
        network,
        container,
    )

    // TODO: Review
    override suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit> = incus(
        "network", if (enabled) "attach" else "detach",
        id,
        id,
    )

    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String,
    ): Result<Unit> = runSuspendingCatching {
        suspend fun addNetworkForward(addr: String) = incus(
            "network", "forward",
            "port", "add",
            id,
            "127.0.0.1",
            protocol,
            externalPort.toString(),
            addr,
            internalPort.toString(),
        ).getOrThrow()

        val info = containerInfo(id).getOrThrow()

        addNetworkForward(info.ipv4)
        addNetworkForward(info.ipv6)
    }

    override suspend fun removePortForwarding(
        id: String,
        externalPort: Int,
        protocol: String,
    ): Result<Unit> = incus(
        "network", "forward",
        "port", "remove",
        id,
        protocol,
        externalPort.toString(),
    )

    override suspend fun startContainer(id: String): Result<Unit> = incus("start", id)

    override suspend fun stopContainer(id: String): Result<Unit> = incus("stop", id)

    // TODO: More detailed progress
    override fun wipe(): Flow<ResultProgress<Unit>> = flowCatching {
        emit(ResultProgress.proceed(0f, "Removing containers..."))
        getContainers().getOrThrow().forEach { removeContainer(it.id).getOrThrow() }
        emit(ResultProgress.proceed(0.6f, "Removing networks..."))
        getNetworks().getOrThrow().forEach { (name, _) -> removeNetwork(name) }
        emit(ResultProgress.proceed(0.7f, "Removing remaining container data..."))
        exec("rm", "-rf", "/opt/intervirt/*").getOrThrow()
        emit(ResultProgress.success(Unit))
    }

    // TODO: More detailed progress
    override fun update(): Flow<ResultProgress<Unit>> = flowCatching {
        emit(ResultProgress.proceed(0f, "Updating package indices..."))
        exec("apk", "update").getOrThrow()
        emit(ResultProgress.proceed(0.5f, "Upgrading guest..."))
        exec("apk", "upgrade").getOrThrow()
        emit(ResultProgress.success(Unit))
    }

    override suspend fun shutdown(): Result<Unit> = exec("poweroff").map { }

    override suspend fun reboot(): Result<Unit> = exec("reboot").map { }

    override suspend fun getVersion(): Result<String> = Result.success(CURRENT_VERSION)

    override suspend fun getContainers(): Result<List<ContainerInfo>> = runSuspendingCatching {
        incusGet<List<IncusInstanceInfo>>("list").getOrThrow()
            .map { list ->
                list.run {
                    val network =
                        state.network[name] ?: error("Expected network with name $name, but none found in $name")
                    val portForwardings = networkForwardInfo(name).getOrThrow()
                        .ports
                        .map {
                            PortForwarding(it.protocol, it.listenPort.toInt(), it.targetPort.toInt())
                        }

                    ContainerInfo(
                        id = name,
                        ipv4 = network.addresses.first { it.family == "inet4" }.address,
                        ipv6 = network.addresses.first { it.family == "inet6" }.address,
                        mac = network.hwaddr,
                        internet = state.network["eth0"] != null,
                        image = "${config.os}/${config.release}", // TODO: Image should be valid
                        portForwardings = portForwardings,
                        running = status == "Running",
                    )
                }
            }
    }

    override suspend fun addNetwork(name: String): Result<Unit> = incus("network", "create", "intervirt-$name")

    override suspend fun removeNetwork(name: String): Result<Unit> = incus("network", "delete", "intervirt-$name")

    override suspend fun getNetworks(): Result<Map<String, Network>> = incusGet<List<IncusNetwork>>("network", "list")
        .map { networks ->
            networks.associate { network ->
                network.name to network.usedBy.mapNotNull { usedBy ->
                    usedBy.substringAfter("/instances/")
                        .substringBefore("?")
                        .takeIf { it.isNotBlank() }
                }
            }
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


    private suspend fun containerInfo(id: String): Result<ContainerInfo> = getContainers().map { list ->
        list.first { it.id == id }
    }

    private suspend fun networkForwardInfo(network: String): Result<IncusNetworkForwardInfo> =
        incusGet<List<IncusNetworkForwardInfo>>("network", "forward", "list", network).mapCatching { it.first() }

    private suspend fun incus(vararg args: String): Result<Unit> = incusExec(args).map { }

    private suspend inline fun <reified T> incusGet(vararg args: String): Result<T> =
        incusExec(args).map { defaultJson.decodeFromString(it) }

    private suspend fun incusExec(args: Array<out String>): Result<String> = exec("incus", *args)

    private suspend fun exec(vararg command: String) = client.runCommand(*command).mapCatching {
        it.getCommandResult()
            .asResult()
            .getOrThrow()
    }

}
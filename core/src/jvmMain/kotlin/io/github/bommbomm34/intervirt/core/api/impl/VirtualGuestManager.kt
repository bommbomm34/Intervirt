/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.right
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.AgentInfo

import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.ContainerInfo
import io.github.bommbomm34.intervirt.core.data.agent.Network
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.annotations.VisibleForTesting
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


// Virtual Guest Manager
class VirtualGuestManager(private val delay: Duration = 500.milliseconds) : GuestManager {
    private val containers = mutableListOf<Container>()
    private val networks = mutableMapOf<String, MutableList<String>>()

    context(_: Raise<Failure>)
    override suspend fun addContainer(
        id: String,
        ipv4: String,
        ipv6: String,
        mac: String,
        internet: Boolean,
        image: String,
    ): Flow<ResultProgress<Unit>> {
        delay()
        containers.add(Container(id, ipv4, ipv6, mac, internet, image))
        return flowOf(ResultProgress.result(Unit.right()))
    }

    context(_: Raise<Failure>)
    override suspend fun removeContainer(id: String) {
        delay()
        val removed = containers.removeAll { it.id == id }
        if (!removed) raise(Failure.NotFound("Container $id not found."))
    }

    context(_: Raise<Failure>)
    override suspend fun setIpv4(id: String, newIP: String) {
        delay()
        containers.first { it.id == id }.ipv4 = newIP
    }

    context(_: Raise<Failure>)
    override suspend fun setIpv6(id: String, newIP: String) {
        delay()
        containers.first { it.id == id }.ipv6 = newIP
    }

    context(_: Raise<Failure>)
    override suspend fun connect(container: String, network: String) {
        delay()
        if (!container.exists()) raise(Failure.NotFound("Container $container doesn't exist."))
        networks[network]?.add(container) ?: raise(Failure.NotFound("Network $network doesn't exist."))
    }

    context(_: Raise<Failure>)
    override suspend fun disconnect(container: String, network: String) {
        delay()
        if (!container.exists()) raise(Failure.NotFound("Container $container doesn't exist."))
        networks[network]?.remove(container)
            ?: raise(Failure.NotFound("Network $network doesn't exist."))
    }

    context(_: Raise<Failure>)
    override suspend fun setInternetAccess(id: String, enabled: Boolean) {
        delay()
        getContainerByID(id).internet = enabled
    }

    context(_: Raise<Failure>)
    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String,
    ) {
        delay()
        getContainerByID(id).portForwardings.add(
            PortForwarding(
                protocol = protocol,
                externalPort = externalPort,
                internalPort = internalPort,
            ),
        )
    }

    context(_: Raise<Failure>)
    override suspend fun removePortForwarding(
        id: String,
        externalPort: Int,
        protocol: String,
    ) {
        delay()
        containers.forEach { container ->
            container.portForwardings.removeIf { it.externalPort == externalPort && it.protocol == protocol }
        }
    }

    context(_: Raise<Failure>)
    override suspend fun startContainer(id: String) {
        delay()
        getContainerByID(id).running = true
    }

    context(_: Raise<Failure>)
    override suspend fun stopContainer(id: String) {
        delay()
        getContainerByID(id).running = false
    }

    context(_: Raise<Failure>)
    override fun wipe(): Flow<ResultProgress<Unit>> = flow {
        delay()
        emit(ResultProgress.proceed(0.2f, "Deleting containers..."))
        containers.clear()
        delay()
        emit(ResultProgress.proceed(0.5f, "Deleting networks..."))
        networks.clear()
        emit(ResultProgress.success(Unit))
    }

    context(_: Raise<Failure>)
    override fun update(): Flow<ResultProgress<Unit>> = flow {
        delay()
        emit(ResultProgress.success(Unit))
    }

    context(_: Raise<Failure>)
    override suspend fun shutdown() {}

    context(_: Raise<Failure>)
    override suspend fun reboot() {}

    context(_: Raise<Failure>)
    override suspend fun getInfo() = AgentInfo(
        version = CURRENT_VERSION,
        ipv4Subnet = IPV4_SUBNET,
        ipv6Subnet = IPV6_SUBNET,
    )

    context(_: Raise<Failure>)
    override suspend fun getContainers(): List<ContainerInfo> {
        delay()
        return containers.map {
            ContainerInfo(
                id = it.id,
                ipv4 = it.ipv4,
                ipv6 = it.ipv6,
                mac = it.mac,
                internet = it.internet,
                image = it.image,
                portForwardings = it.portForwardings,
                running = it.running,
            )
        }
    }

    context(_: Raise<Failure>)
    override suspend fun addNetwork(name: String) {
        networks[name] = mutableListOf()
    }

    context(_: Raise<Failure>)
    override suspend fun removeNetwork(name: String) {
        networks.remove(name)
    }

    context(_: Raise<Failure>)
    override suspend fun getNetworks(): Map<String, Network> = networks

    private fun getContainerByID(id: String) = containers.first { it.id == id }

    private fun String.exists() = containers.any { it.id == this }

    private suspend fun delay() = kotlinx.coroutines.delay(delay)

    context(_: Raise<Failure>)
    override suspend fun close() {} // Nothing to close

    companion object {
        @VisibleForTesting
        internal val IPV4_SUBNET: IPAddress = IPAddressString("192.168.73.0/24").getAddress()

        @VisibleForTesting
        internal val IPV6_SUBNET: IPAddress = IPAddressString("fd42:7c9a:15e3::/48").getAddress()
    }
}

private data class Container(
    val id: String,
    var ipv4: String,
    var ipv6: String,
    val mac: String,
    var internet: Boolean,
    val image: String,
    val portForwardings: MutableList<PortForwarding> = mutableListOf(),
    var running: Boolean = true,
)

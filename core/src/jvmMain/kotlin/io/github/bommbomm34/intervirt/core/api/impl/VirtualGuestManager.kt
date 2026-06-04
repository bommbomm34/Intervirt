/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import arrow.core.left
import arrow.core.right
import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.ContainerInfo
import io.github.bommbomm34.intervirt.core.data.agent.Network
import io.github.bommbomm34.intervirt.core.exceptions.NotFoundException
import io.github.bommbomm34.intervirt.core.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


// Virtual Guest Manager
class VirtualGuestManager(private val delay: Duration = 500.milliseconds) : GuestManager {
    private val containers = mutableListOf<Container>()
    private val networks = mutableMapOf<String, MutableList<String>>()

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
        return Unit.right().toFlow()
    }

    override suspend fun removeContainer(id: String): AppResult<Unit> {
        delay()
        val removed = containers.removeIf { it.id == id }
        return if (removed) Unit.right() else Failure.NotFound("Container $id not found.").left()
    }

    override suspend fun setIpv4(id: String, newIP: String): AppResult<Unit> {
        delay()
        containers.first { it.id == id }.ipv4 = newIP
        return Unit.right()
    }

    override suspend fun setIpv6(id: String, newIP: String): AppResult<Unit> {
        delay()
        containers.first { it.id == id }.ipv6 = newIP
        return Unit.right()
    }

    override suspend fun connect(container: String, network: String): AppResult<Unit> {
        delay()
        if (!container.exists()) return Failure.NotFound("Container $container doesn't exist.").left()
        networks[network]?.add(container) ?: return Failure.NotFound("Network $network doesn't exist.").left()
        return Unit.right()
    }

    override suspend fun disconnect(container: String, network: String): AppResult<Unit> {
        delay()
        if (!container.exists()) return Failure.NotFound("Container $container doesn't exist.").left()
        networks[network]?.remove(container)
            ?: return Failure.NotFound("Network $network doesn't exist.").left()
        return Unit.right()
    }

    override suspend fun setInternetAccess(id: String, enabled: Boolean): AppResult<Unit> {
        delay()
        getContainerByID(id).internet = enabled
        return Unit.right()
    }

    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String,
    ): AppResult<Unit> {
        delay()
        getContainerByID(id).portForwardings.add(
            PortForwarding(
                protocol = protocol,
                externalPort = externalPort,
                internalPort = internalPort,
            ),
        )
        return Unit.right()
    }

    override suspend fun removePortForwarding(
        id: String,
        externalPort: Int,
        protocol: String,
    ): AppResult<Unit> {
        delay()
        containers.forEach { container ->
            container.portForwardings.removeIf { it.externalPort == externalPort && it.protocol == protocol }
        }
        return Unit.right()
    }

    override suspend fun startContainer(id: String): AppResult<Unit> {
        delay()
        getContainerByID(id).running = true
        return Unit.right()
    }

    override suspend fun stopContainer(id: String): AppResult<Unit> {
        delay()
        getContainerByID(id).running = false
        return Unit.right()
    }

    override fun wipe(): Flow<ResultProgress<Unit>> = flow {
        delay()
        emit(ResultProgress.proceed(0.2f, "Deleting containers..."))
        containers.clear()
        delay()
        emit(ResultProgress.proceed(0.5f, "Deleting networks..."))
        networks.clear()
        emit(ResultProgress.success(Unit))
    }

    override fun update(): Flow<ResultProgress<Unit>> = flow {
        delay()
        emit(ResultProgress.success(Unit))
    }

    override suspend fun shutdown() = Unit.right()

    override suspend fun reboot() = Unit.right()

    override suspend fun getVersion() = CURRENT_VERSION.right()

    override suspend fun getContainers(): AppResult<List<ContainerInfo>> {
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
        }.right()
    }

    override suspend fun addNetwork(name: String): AppResult<Unit> {
        networks[name] = mutableListOf()
        return Unit.right()
    }

    override suspend fun removeNetwork(name: String): AppResult<Unit> {
        networks.remove(name)
        return Unit.right()
    }

    override suspend fun getNetworks(): AppResult<Map<String, Network>> = networks.right()

    private fun getContainerByID(id: String) = containers.first { it.id == id }

    private fun String.exists() = containers.any { it.id == this }

    private suspend fun delay() = kotlinx.coroutines.delay(delay)

    override suspend fun close() = Unit.right() // Nothing to close
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

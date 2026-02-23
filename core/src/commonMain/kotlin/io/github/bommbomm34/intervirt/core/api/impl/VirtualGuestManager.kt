package io.github.bommbomm34.intervirt.core.api.impl

import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.exceptions.NotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val DELAY = 500L

// Virtual Guest Manager
class VirtualGuestManager : GuestManager {
    private val containers = mutableListOf<Container>()
    private val connections = mutableListOf<ContainerConnection>()

    override suspend fun addContainer(
        id: String,
        initialIpv4: String,
        initialIpv6: String,
        mac: String,
        internet: Boolean,
        image: String,
    ): Result<Unit> {
        delay()
        containers.add(Container(id, initialIpv4, initialIpv6, mac, internet, image))
        return Result.success(Unit)
    }

    override suspend fun removeContainer(id: String): Result<Unit> {
        delay()
        val removed = containers.removeIf { it.id == id }
        return if (removed) Result.success(Unit) else Result.failure(NotFoundException("Container $id not found."))
    }

    override suspend fun setIpv4(id: String, newIP: String): Result<Unit> = runCatching {
        delay()
        containers.first { it.id == id }.ipv4 = newIP
    }

    override suspend fun setIpv6(id: String, newIP: String): Result<Unit> = runCatching {
        delay()
        containers.first { it.id == id }.ipv6 = newIP
    }

    override suspend fun connect(id1: String, id2: String): Result<Unit> {
        delay()
        if (!id1.exists()) return Result.failure(NotFoundException("Container $id1 doesn't exist."))
        if (!id2.exists()) return Result.failure(NotFoundException("Container $id2 doesn't exist."))
        connections.add(ContainerConnection(id1, id2))
        return Result.success(Unit)
    }

    override suspend fun disconnect(id1: String, id2: String): Result<Unit> {
        delay()
        if (!id1.exists()) return Result.failure(NotFoundException("Container $id1 doesn't exist."))
        if (!id2.exists()) return Result.failure(NotFoundException("Container $id2 doesn't exist."))
        connections.remove(ContainerConnection(id1, id2))
        return Result.success(Unit)
    }

    override suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit> = runCatching {
        delay()
        getContainerByID(id).internet = enabled
    }

    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String,
    ): Result<Unit> = runCatching {
        delay()
        getContainerByID(id).portForwardings.add(
            PortForwarding(
                protocol = protocol,
                externalPort = externalPort,
                internalPort = internalPort,
            ),
        )
    }

    override suspend fun removePortForwarding(
        externalPort: Int,
        protocol: String,
    ): Result<Unit> = runCatching {
        delay()
        containers.forEach { container ->
            container.portForwardings.removeIf { it.externalPort == externalPort && it.protocol == protocol }
        }
    }

    override suspend fun startContainer(id: String): Result<Unit> = runCatching {
        delay()
        getContainerByID(id).running = true
    }

    override suspend fun stopContainer(id: String): Result<Unit> = runCatching {
        delay()
        getContainerByID(id).running = false
    }

    override fun wipe(): Flow<ResultProgress<Unit>> = flow {
        delay()
        emit(ResultProgress.proceed(0.2f, "Deleting containers..."))
        containers.clear()
        delay()
        emit(ResultProgress.proceed(0.5f, "Deleting connections..."))
        connections.clear()
        emit(ResultProgress.success(Unit))
    }

    override fun update(): Flow<ResultProgress<Unit>> = flow {
        delay()
        emit(ResultProgress.success(Unit))
    }

    override suspend fun shutdown() =
        Result.failure<Unit>(NotImplementedError("Shutdown through VirtualGuestManager isn't possible."))

    override suspend fun reboot() = Result.success(Unit)

    override suspend fun getVersion() = Result.success(CURRENT_VERSION)

    private fun getContainerByID(id: String) = containers.first { it.id == id }

    private fun String.exists() = containers.any { it.id == this }

    private suspend fun delay() = kotlinx.coroutines.delay(DELAY)

    override suspend fun close() = Result.success(Unit) // Nothing to close
}

private data class Container(
    val id: String,
    var ipv4: String,
    var ipv6: String,
    val mac: String,
    var internet: Boolean,
    val image: String,
    val portForwardings: MutableList<PortForwarding> = mutableListOf(),
    var running: Boolean = false,
)

private data class ContainerConnection(
    val id1: String,
    val id2: String,
)
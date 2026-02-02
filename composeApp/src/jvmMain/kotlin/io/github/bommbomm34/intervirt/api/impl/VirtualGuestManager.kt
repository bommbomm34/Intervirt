package io.github.bommbomm34.intervirt.api.impl

import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.api.FileManager
import io.github.bommbomm34.intervirt.api.GuestManager
import io.github.bommbomm34.intervirt.data.*
import io.github.bommbomm34.intervirt.exceptions.NotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlin.io.encoding.Base64

// Virtual Guest Manager
class VirtualGuestManager : GuestManager {
    private val containers = mutableListOf<Container>()
    private val connections = mutableListOf<DeviceConnection>()

    override suspend fun addContainer(
        id: String,
        initialIpv4: String,
        initialIpv6: String,
        mac: String,
        internet: Boolean,
        image: String
    ): Result<Unit> {
        containers.add(Container(id, initialIpv4, initialIpv6, mac, internet, image))
        return Result.success(Unit)
    }

    override suspend fun removeContainer(id: String): Result<Unit> {
        val removed = containers.removeIf { it.id == id }
        return if (removed) Result.success(Unit) else Result.failure(NotFoundException("Container $id not found."))
    }

    override suspend fun setIpv4(id: String, newIP: String): Result<Unit> = runCatching {
        containers.first { it.id == id }.ipv4 = newIP
    }

    override suspend fun setIpv6(id: String, newIP: String): Result<Unit> = runCatching {
        containers.first { it.id == id }.ipv6 = newIP
    }

    override suspend fun connect(id1: String, id2: String): Result<Unit> {
        if (!id1.exists()) return Result.failure(NotFoundException("Container $id1 doesn't exist."))
        if (!id2.exists()) return Result.failure(NotFoundException("Container $id2 doesn't exist."))
        connections.add(id1.toDevice() connect id2.toDevice())
        return Result.success(Unit)
    }

    override suspend fun disconnect(id1: String, id2: String): Result<Unit> {
        if (!id1.exists()) return Result.failure(NotFoundException("Container $id1 doesn't exist."))
        if (!id2.exists()) return Result.failure(NotFoundException("Container $id2 doesn't exist."))
        connections.remove(id1.toDevice() connect id2.toDevice())
        return Result.success(Unit)
    }

    override suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit> = runCatching {
        getContainerByID(id).internet = enabled
    }

    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String
    ): Result<Unit> = runCatching {
        getContainerByID(id).portForwardings.add(
            PortForwarding(
                protocol = protocol,
                hostPort = externalPort,
                guestPort = internalPort
            )
        )
    }

    override suspend fun removePortForwarding(
        externalPort: Int,
        protocol: String
    ): Result<Unit> = runCatching {
        containers.forEach { container ->
            container.portForwardings.removeIf { it.hostPort == externalPort && it.protocol == protocol }
        }
    }

    override fun wipe(): Flow<ResultProgress<Unit>> = flow {
        emit(ResultProgress.proceed(0.2f, "Deleting containers..."))
        containers.clear()
        emit(ResultProgress.proceed(0.5f, "Deleting connections..."))
        connections.clear()
        emit(ResultProgress.success(Unit))
    }

    override fun update(): Flow<ResultProgress<Unit>> = flow {
        emit(ResultProgress.success(Unit))
    }

    override suspend fun shutdown() = Result.success(Unit)

    override suspend fun reboot() = Result.success(Unit)

    override suspend fun getVersion() = Result.success(CURRENT_VERSION)

    private fun getContainerByID(id: String) = containers.first { it.id == id }

    private fun String.exists() = containers.any { it.id == this }
}

private data class Container(
    val id: String,
    var ipv4: String,
    var ipv6: String,
    val mac: String,
    var internet: Boolean,
    val image: String,
    val portForwardings: MutableList<PortForwarding> = mutableListOf()
)
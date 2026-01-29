package io.github.bommbomm34.intervirt.api.impl

import io.github.bommbomm34.intervirt.api.FileManager
import io.github.bommbomm34.intervirt.api.GuestManager
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.exceptions.NotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlin.io.encoding.Base64

// Virtual Guest Manager
class VirtualGuestManager : GuestManager {
    private val containers = mutableListOf<Container>()
    
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

    override suspend fun downloadFile(
        id: String,
        path: String,
        fileManager: FileManager
    ): Result<File> = runCatching {
        val file = File(path)
        val targetFile = fileManager.getFile("cache/${Base64.encode(path.toByteArray())}.${file.extension}")
        file.copyTo(targetFile)
        return@runCatching targetFile
    }

    override fun uploadFile(
        id: String,
        file: File,
        path: String,
        fileManager: FileManager
    ): Flow<ResultProgress<Unit>> = flow {
        emit(ResultProgress.proceed(0.1f, "Uploading file..."))
        runCatching {
            file.copyTo(File(path))
        }.fold(
            onSuccess = { emit(ResultProgress.success(Unit)) },
            onFailure = { emit(ResultProgress.failure(it)) }
        )
    }

    override suspend fun setIpv4(id: String, newIP: String): Result<Unit> {
        containers.firstOrNull { it.id == id }?.ipv4 = newIP
    }

    override suspend fun setIpv6(id: String, newIP: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun connect(id1: String, id2: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(id1: String, id2: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun removePortForwarding(
        externalPort: Int,
        protocol: String
    ): Result<Unit> {
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
}

private data class Container(
    val id: String,
    var ipv4: String,
    var ipv6: String,
    val mac: String,
    var internet: Boolean,
    val image: String
)
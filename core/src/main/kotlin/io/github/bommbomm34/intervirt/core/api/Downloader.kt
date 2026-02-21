package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.exceptions.DownloadException
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class Downloader(
    private val fileManager: FileManager,
    private val appEnv: AppEnv,
    private val client: HttpClient,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun checkUpdates(): Result<List<Component>> = runSuspendingCatching {
        buildList {
            if (appEnv.QEMU_ZIP_HASH_URL.fetch()
                    .getOrThrow() != appEnv.CURRENT_QEMU_HASH
            ) add(Component.QEMU)
            if (appEnv.VM_DISK_HASH_URL.fetch()
                    .getOrThrow() != appEnv.CURRENT_DISK_HASH
            ) add(Component.VM_DISK)
        }
    }

    fun upgrade(components: List<Component>): Flow<ResultProgress<String>> = flow {
        val proportion = 1f / components.size
        components.forEachIndexed { i, component ->
            downloadComponent(component).collect {
                emit(it.clone(percentage = proportion * it.percentage + i * proportion))
            }
        }
    }

    fun downloadComponent(component: Component, update: Boolean = false): Flow<ResultProgress<String>> =
        when (component) {
            Component.QEMU -> downloadQemu(update)
            Component.VM_DISK -> downloadAlpineDisk(update)
        }

    fun downloadQemu(update: Boolean = false): Flow<ResultProgress<String>> {
        logger.debug { "Downloading QEMU" }
        return downloadQemuZip(update)
    }

    fun downloadAlpineDisk(update: Boolean = false): Flow<ResultProgress<String>> = flow {
        logger.debug { "Downloading disk" }
        if (!appEnv.DISK_INSTALLED || update) {
            // Invalidate previous installation
            appEnv.DISK_INSTALLED = false
            val hashRes = appEnv.VM_DISK_HASH_URL.fetch()
            val file = fileManager.downloadFile(appEnv.VM_DISK_URL, "alpine-linux.qcow2", fileManager.getFile("disk"))
            hashRes.fold(
                onSuccess = { hash ->
                    file.collect { resultProgress ->
                        if (resultProgress is ResultProgress.Result) {
                            resultProgress.result.fold(
                                onSuccess = {
                                    emit(ResultProgress.success("Download succeeded"))
                                    appEnv.DISK_INSTALLED = true
                                    appEnv.CURRENT_DISK_HASH = hash
                                },
                                onFailure = {
                                    emit(ResultProgress.failure(it))
                                },
                            )
                        } else {
                            emit(
                                ResultProgress.proceed(
                                    resultProgress.percentage,
                                    "Downloading VM...",
                                ),
                            )
                        }
                    }
                },
                onFailure = { emit(ResultProgress.failure(it)) },
            )
        } else {
            logger.debug { "Already installed disk" }
            emit(ResultProgress.success("Successfully downloaded VM"))
        }
    }

    private fun downloadQemuZip(update: Boolean = false): Flow<ResultProgress<String>> = flow {
        if (!appEnv.QEMU_INSTALLED || update) {
            withContext(Dispatchers.IO) {
                // Wipe previous installation if available
                fileManager.getFile("qemu").listFiles().forEach { it.delete() }
                // Invalidate previous installation
                appEnv.QEMU_INSTALLED = false
                // Install fresh QEMU
                val hashRes = appEnv.QEMU_ZIP_HASH_URL.fetch()
                val file = fileManager.downloadFile(appEnv.QEMU_ZIP_URL, "qemu-portable.zip")
                hashRes.fold(
                    onSuccess = { hash ->
                        file.collect { resultProgress ->
                            if (resultProgress is ResultProgress.Result) {
                                resultProgress.result.fold(
                                    onSuccess = { zipFile ->
                                        fileManager.extractZip(zipFile, fileManager.getFile("qemu"))
                                            .onFailure { emit(ResultProgress.failure(it)) }
                                        appEnv.QEMU_INSTALLED = true
                                        appEnv.CURRENT_QEMU_HASH = hash
                                        emit(
                                            ResultProgress.success("Successfully downloaded QEMU"),
                                        )
                                    },
                                    onFailure = {
                                        emit(ResultProgress.failure(DownloadException(it.localizedMessage)))
                                    },
                                )
                            } else {
                                emit(
                                    ResultProgress.proceed(
                                        resultProgress.percentage,
                                        "Downloading QEMU...",
                                    ),
                                )
                            }
                        }
                    },
                    onFailure = { emit(ResultProgress.failure(it)) },
                )
            }
        } else {
            logger.debug { "Already installed QEMU" }
            emit(ResultProgress.success("Successfully downloaded QEMU"))
        }
    }

    private suspend fun String.fetch(): Result<String> {
        logger.debug { "Fetching has from url $this" }
        val res = client.get(this)
        return if (res.status == HttpStatusCode.OK) {
            val hash = res.bodyAsText()
            logger.debug { "Successfully fetched hash: $hash" }
            Result.success(hash)
        } else {
            val exception = IllegalStateException(res.status.description)
            logger.error(exception) { "Failed acquiring hash from url $this" }
            Result.failure(exception)
        }
    }

    enum class Component(val readableName: String) {
        VM_DISK("VM Disk"),
        QEMU("QEMU")
    }
}
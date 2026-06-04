/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.left
import arrow.core.raise.context.bind
import arrow.core.raise.either
import arrow.core.right
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.error
import io.github.bommbomm34.intervirt.core.exceptions.DownloadException
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.list

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
    private val logger = appEnv.getLogger(Downloader::class)

    suspend fun checkUpdates(): AppResult<List<Component>> = either {
        buildList {
            if (appEnv.QEMU_ZIP_HASH_URL.fetch()
                    .bind() != appEnv.CURRENT_QEMU_HASH
            ) add(Component.QEMU)
            if (appEnv.VM_DISK_HASH_URL.fetch()
                    .bind() != appEnv.CURRENT_DISK_HASH
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
                ifRight = { hash ->
                    file.collect { resultProgress ->
                        if (resultProgress is ResultProgress.Result) {
                            logger.debug { "Disk download succeeded" }
                            resultProgress.result.fold(
                                ifRight = {
                                    emit(ResultProgress.success("Download succeeded"))
                                    appEnv.DISK_INSTALLED = true
                                    appEnv.CURRENT_DISK_HASH = hash
                                },
                                ifLeft = {
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
                ifLeft = { emit(ResultProgress.failure(it)) },
            )
        } else {
            logger.debug { "Already installed disk" }
            emit(ResultProgress.success("Successfully downloaded VM"))
        }
    }

    private fun downloadQemuZip(update: Boolean = false): Flow<ResultProgress<String>> = flow {
        logger.debug { "Downloading QEMU" }
        if (!appEnv.QEMU_INSTALLED || update) {
            withContext(Dispatchers.IO) {
                // Wipe previous installation if available
                fileManager.getFile("qemu").list().forEach { it.delete() }
                // Invalidate previous installation
                appEnv.QEMU_INSTALLED = false
                // Install fresh QEMU
                val hashRes = appEnv.QEMU_ZIP_HASH_URL.fetch()
                val file = fileManager.downloadFile(appEnv.QEMU_ZIP_URL, "qemu-portable.zip")
                hashRes.fold(
                    ifRight = { hash ->
                        file.collect { resultProgress ->
                            if (resultProgress is ResultProgress.Result) {
                                logger.debug { "Successfully downloaded QEMU" }
                                resultProgress.result.fold(
                                    ifRight = { zipFile ->
                                        fileManager.extractZip(zipFile, fileManager.getFile("qemu"))
                                            .onLeft { emit(ResultProgress.failure(it)) }
                                        appEnv.QEMU_INSTALLED = true
                                        appEnv.CURRENT_QEMU_HASH = hash
                                        emit(
                                            ResultProgress.success("Successfully downloaded QEMU"),
                                        )
                                    },
                                    ifLeft = {
                                        emit(ResultProgress.failure(Failure.Download(it.message)))
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
                    ifLeft = { emit(ResultProgress.failure(it)) },
                )
            }
        } else {
            logger.debug { "Already installed QEMU" }
            emit(ResultProgress.success("Successfully downloaded QEMU"))
        }
    }

    private suspend fun String.fetch(): AppResult<String> {
        logger.debug { "Fetching has from url $this" }
        val res = client.get(this)
        return if (res.status == HttpStatusCode.OK) {
            val hash = res.bodyAsText()
            logger.debug { "Successfully fetched hash: $hash" }
            hash.right()
        } else {
            val failure = Failure.Download(res.status.description)
            logger.error(failure) { "Failed acquiring hash from url $this" }
            failure.left()
        }
    }

    enum class Component(val readableName: String) {
        VM_DISK("VM Disk"),
        QEMU("QEMU")
    }
}

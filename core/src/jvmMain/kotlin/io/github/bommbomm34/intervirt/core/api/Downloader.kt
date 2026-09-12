/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.bind
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import arrow.core.right
import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.atomic.getValue
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.error
import io.github.bommbomm34.intervirt.core.util.ext.flowCatching
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.toJavaPath
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.toKotlinxIoPath
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively

class Downloader(
    private val fileManager: FileManager,
    private val client: HttpClient,
    envHolder: AppEnvHolder,
    private val envUpdater: AppEnvHolder,
) {
    val appEnv by envHolder
    private val logger = appEnv.getLogger(Downloader::class)

    context(_: Raise<Failure>)
    suspend fun checkUpdates(): List<Component> {
        return buildList {
            if (appEnv.qemuZipHashUrl.fetch().bind()
                     != appEnv.currentQemuHash
            ) add(Component.QEMU)
            if (appEnv.vmDiskHashUrl.fetch().bind()
                     != appEnv.currentDiskHash
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

    fun downloadAlpineDisk(update: Boolean = false): Flow<ResultProgress<String>> = flowCatching {
        logger.debug { "Downloading disk" }
        if (!appEnv.diskInstalled || update) {
            // Delete previous disk
            val destination = fileManager.getFile("disk")
            destination.deleteContentsRecursively()
            // Invalidate previous installation
            envUpdater set appEnv.copy(diskInstalled = false)
            val hash = appEnv.vmDiskHashUrl.fetch().bind()
            val file = fileManager.downloadFile(appEnv.vmDiskUrl, "alpine-linux.qcow2", destination)
            file.collect { resultProgress ->
                if (resultProgress is ResultProgress.Result) {
                    logger.debug { "Disk download succeeded" }
                    resultProgress.result.bind()
                    emit(ResultProgress.success("Download succeeded"))
                    envUpdater set appEnv.copy(
                        diskInstalled = true,
                        currentDiskHash = hash,
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
        } else {
            logger.debug { "Already installed disk" }
            emit(ResultProgress.success("Successfully downloaded VM"))
        }
    }

    private fun downloadQemuZip(update: Boolean = false): Flow<ResultProgress<String>> = flowCatching {
        logger.debug { "Downloading QEMU" }
        if (!appEnv.qemuInstalled || update) {
            withContext(Dispatchers.IO) {
                // Wipe previous installation if available
                fileManager.getFile("qemu").deleteContentsRecursively()
                // Invalidate previous installation
                envUpdater set appEnv.copy(qemuInstalled = false)
                // Install fresh QEMU
                val hash = appEnv.qemuZipHashUrl.fetch().bind()
                val file = fileManager.downloadFile(appEnv.qemuZipUrl, "qemu-portable.zip")
                file.collect { resultProgress ->
                    if (resultProgress is ResultProgress.Result) {
                        logger.debug { "Successfully downloaded QEMU" }
                        val zipFile = resultProgress.result.bind()

                        fileManager.extractZip(zipFile, fileManager.getFile("qemu")).bind()
                        envUpdater set appEnv.copy(
                            qemuInstalled = true,
                            currentQemuHash = hash,
                        )
                        emit(
                            ResultProgress.success("Successfully downloaded QEMU"),
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
            }
        } else {
            logger.debug { "Already installed QEMU" }
            emit(ResultProgress.success("Successfully downloaded QEMU"))
        }
    }

    private suspend fun String.fetch(): Either<Failure, String> {
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

    @OptIn(ExperimentalPathApi::class)
    context(_: Raise<Failure>)
    private fun PlatformFile.deleteContentsRecursively() {
        for (file in list()) {
            try {
                file.toJavaPath().deleteRecursively()
            } catch (e: IOException) {
                val message = e.message.orEmpty()
                raise(Failure.FailedFileOperation(file, message))
            }
        }
    }

    enum class Component(val readableName: String) {
        VM_DISK("VM Disk"),
        QEMU("QEMU")
    }
}

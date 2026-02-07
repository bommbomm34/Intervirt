package io.github.bommbomm34.intervirt.api

import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.data.AppEnv
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.exceptions.DownloadException
import io.github.bommbomm34.intervirt.exceptions.ZipExtractionException
import io.github.bommbomm34.intervirt.runSuspendingCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import org.jetbrains.compose.resources.getString

class Downloader(
    private val preferences: Preferences,
    private val fileManager: FileManager,
    private val appEnv: AppEnv
) {
    private val logger = KotlinLogging.logger { }

    suspend fun checkUpdates(): Result<List<Component>> = runSuspendingCatching {
        buildList {
            if (appEnv.qemuZipHashUrl.fetch().getOrThrow() != preferences.env("CURRENT_QEMU_HASH")) add(Component.QEMU)
            if (appEnv.vmDiskHashUrl.fetch()
                    .getOrThrow() != preferences.env("CURRENT_DISK_HASH")
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
        if (!preferences.env("DISK_INSTALLED").toBoolean() || update) {
            // Invalidate previous installation
            preferences.saveString("DISK_INSTALLED", "false")
            val hashRes = appEnv.vmDiskHashUrl.fetch()
            val file = fileManager.downloadFile(appEnv.vmDiskUrl, "alpine-linux.qcow2", fileManager.getFile("disk"))
            hashRes.fold(
                onSuccess = { hash ->
                    file.collect { resultProgress ->
                        if (resultProgress is ResultProgress.Result) {
                            resultProgress.result.onFailure {
                                emit(ResultProgress.failure(it))
                            }.onSuccess {
                                emit(ResultProgress.success(getString(Res.string.download_succeeded)))
                                preferences.saveString("DISK_INSTALLED", "true")
                                preferences.saveString("CURRENT_DISK_HASH", hash)
                            }
                        } else {
                            emit(
                                ResultProgress.proceed(
                                    resultProgress.percentage,
                                    getString(Res.string.downloading, "VM")
                                )
                            )
                        }
                    }
                },
                onFailure = { emit(ResultProgress.failure(it)) }
            )
        } else {
            logger.debug { "Already installed disk" }
            emit(ResultProgress.success(getString(Res.string.successful_installation, "VM")))
        }
    }

    private fun downloadQemuZip(update: Boolean = false): Flow<ResultProgress<String>> = flow {
        if (!preferences.env("QEMU_INSTALLED").toBoolean() || update) {
            withContext(Dispatchers.IO) {
                // Wipe previous installation if available
                fileManager.getFile("qemu").listFiles().forEach { it.delete() }
                // Invalidate previous installation
                preferences.saveString("QEMU_INSTALLED", "false")
                // Install fresh QEMU
                val hashRes = appEnv.qemuZipHashUrl.fetch()
                val file = fileManager.downloadFile(appEnv.qemuZipUrl, "qemu-portable.zip")
                hashRes.fold(
                    onSuccess = { hash ->
                        file.collect { resultProgress ->
                            if (resultProgress is ResultProgress.Result) {
                                resultProgress.result.onSuccess { zipFile ->
                                    val zip = ZipFile(zipFile)
                                    try {
                                        logger.debug { "Extracting ${zipFile.name}" }
                                        zip.extractAll(fileManager.getFile("qemu").absolutePath)
                                        preferences.saveString("QEMU_INSTALLED", "true")
                                        preferences.saveString("CURRENT_QEMU_HASH", hash)
                                        emit(
                                            ResultProgress.success(
                                                getString(
                                                    Res.string.download_succeeded,
                                                    "QEMU"
                                                )
                                            )
                                        )
                                    } catch (e: ZipException) {
                                        logger.error { "Error occurred while extracting ${zipFile.name}: ${e.message}" }
                                        emit(
                                            ResultProgress.failure(
                                                ZipExtractionException(
                                                    getString(
                                                        Res.string.error_while_zip_extraction,
                                                        zipFile.name,
                                                        e.localizedMessage
                                                    )
                                                )
                                            )
                                        )
                                    }
                                }.onFailure {
                                    emit(ResultProgress.failure(DownloadException(it.localizedMessage)))
                                }
                            } else {
                                emit(
                                    ResultProgress.proceed(
                                        resultProgress.percentage,
                                        getString(Res.string.downloading, "QEMU")
                                    )
                                )
                            }
                        }
                    },
                    onFailure = { emit(ResultProgress.failure(it)) }
                )
            }
        } else {
            logger.debug { "Already installed QEMU" }
            emit(ResultProgress.success(getString(Res.string.successful_installation, "QEMU")))
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

    enum class Component {
        VM_DISK, QEMU
    }
}
package io.github.bommbomm34.intervirt.api

import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.download_succeeded
import intervirt.composeapp.generated.resources.downloading
import intervirt.composeapp.generated.resources.error_while_zip_extraction
import intervirt.composeapp.generated.resources.successful_installation
import io.github.bommbomm34.intervirt.ALPINE_DISK_URL
import io.github.bommbomm34.intervirt.QEMU_LINUX_URL
import io.github.bommbomm34.intervirt.QEMU_WINDOWS_URL
import io.github.bommbomm34.intervirt.SUPPORTED_ARCHITECTURES
import io.github.bommbomm34.intervirt.data.Os
import io.github.bommbomm34.intervirt.data.Preferences
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.data.getOs
import io.github.bommbomm34.intervirt.exceptions.DownloadException
import io.github.bommbomm34.intervirt.exceptions.UnsupportedArchitectureException
import io.github.bommbomm34.intervirt.exceptions.UnsupportedOsException
import io.github.bommbomm34.intervirt.exceptions.ZipExtractionException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import org.jetbrains.compose.resources.getString

class Downloader(
    val preferences: Preferences,
    val fileManager: FileManager
) {
    val logger = KotlinLogging.logger {  }

    fun downloadQemuWindows(update: Boolean = false): Flow<ResultProgress<String>> = flow {
        logger.debug { "Installing QEMU on Windows..." }
        downloadQemuZip(update, QEMU_WINDOWS_URL).collect { emit(it) }
    }

    fun downloadQemuLinux(update: Boolean = false): Flow<ResultProgress<String>> = flow {
        logger.debug { "Installing QEMU on Linux..." }
        downloadQemuZip(update, QEMU_LINUX_URL).collect { emit(it) }
    }

    fun downloadQemuZip(update: Boolean, url: String): Flow<ResultProgress<String>> = flow {
        if (!preferences.env("QEMU_INSTALLED").toBoolean() || update) {
            // Wipe previous installation if available
            fileManager.getFile("qemu").listFiles().forEach { it.delete() }
            // Install fresh QEMU
            val file = fileManager.downloadFile(url, "qemu-portable.zip")
            file.collect { resultProgress ->
                if (resultProgress.result != null) {
                    resultProgress.result.onSuccess { zipFile ->
                        val zip = ZipFile(zipFile)
                        try {
                            logger.debug { "Extracting ${zipFile.name}" }
                            zip.extractAll(fileManager.getFile("qemu").absolutePath)
                            preferences.saveString("QEMU_INSTALLED", "true")
                            emit(
                                ResultProgress.Companion.success(
                                    getString(
                                        Res.string.download_succeeded,
                                        "QEMU"
                                    )
                                )
                            )
                        } catch (e: ZipException) {
                            logger.error { "Error occurred while extracting ${zipFile.name}: ${e.message}" }
                            emit(
                                ResultProgress.Companion.failure(
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
                        emit(ResultProgress.Companion.failure(DownloadException(it.localizedMessage)))
                    }
                } else {
                    emit(
                        ResultProgress.Companion.proceed(
                            resultProgress.percentage,
                            getString(Res.string.downloading, "QEMU")
                        )
                    )
                }
            }
        } else {
            logger.debug { "Already installed QEMU" }
            emit(ResultProgress.Companion.success(getString(Res.string.successful_installation, "QEMU")))
        }
    }

    fun downloadQemu(update: Boolean = false): Flow<ResultProgress<String>> {
        logger.debug { "Downloading QEMU" }
        return when (getOs()) {
            Os.WINDOWS -> downloadQemuWindows(update)
            Os.LINUX -> downloadQemuLinux(update)
            null -> flow {
                emit(ResultProgress.Companion.failure(UnsupportedOsException()))
            }
        }
    }

    fun downloadAlpineDisk(): Flow<ResultProgress<String>> = flow {
        logger.debug { "Downloading disk" }
        if (!preferences.env("DISK_INSTALLED").toBoolean()) {
            val file = fileManager.downloadFile(ALPINE_DISK_URL, "alpine-linux.qcow2", fileManager.getFile("disk"))
            file.collect { resultProgress ->
                if (resultProgress.result != null) {
                    resultProgress.result.onFailure {
                        emit(ResultProgress.Companion.failure(it))
                    }.onSuccess {
                        emit(ResultProgress.Companion.success(getString(Res.string.download_succeeded)))
                        preferences.saveString("DISK_INSTALLED", "true")
                    }
                } else {
                    emit(
                        ResultProgress.Companion.proceed(
                            resultProgress.percentage,
                            getString(Res.string.downloading, "VM")
                        )
                    )
                }
            }
        } else {
            logger.debug { "Already installed disk" }
            emit(ResultProgress.Companion.success(getString(Res.string.successful_installation, "VM")))
        }
    }

    private fun getDownloadURL(baseURL: String): Result<String> {
        val arch = System.getProperty("os.arch").lowercase()
        logger.debug { "Detected architecture $arch" }
        return when {
            SUPPORTED_ARCHITECTURES.contains(arch) -> Result.success(baseURL + arch)
            else -> Result.failure(UnsupportedArchitectureException())
        }
    }
}
package io.github.bommbomm34.intervirt.setup

import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.QEMU_LINUX_URL
import io.github.bommbomm34.intervirt.QEMU_WINDOWS_URL
import io.github.bommbomm34.intervirt.SUPPORTED_ARCHITECTURES
import io.github.bommbomm34.intervirt.data.FileManager
import io.github.bommbomm34.intervirt.data.OS
import io.github.bommbomm34.intervirt.data.Preferences
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.data.getOS
import io.github.bommbomm34.intervirt.exceptions.DownloadException
import io.github.bommbomm34.intervirt.exceptions.UnsupportedArchitectureException
import io.github.bommbomm34.intervirt.exceptions.UnsupportedOSException
import io.github.bommbomm34.intervirt.exceptions.ZipExtractionException
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import org.jetbrains.compose.resources.getString

object Downloader {
    fun downloadQEMUWindows(update: Boolean = false): Flow<ResultProgress<String>> = flow {
        logger.debug { "Installing QEMU on Windows..." }
        downloadQEMUZIP(update, QEMU_WINDOWS_URL).collect { emit(it) }
    }

    fun downloadQEMULinux(update: Boolean = false): Flow<ResultProgress<String>> = flow {
        logger.debug { "Installing QEMU on Linux..." }
        downloadQEMUZIP(update, QEMU_LINUX_URL).collect { emit(it) }
    }

    fun downloadQEMUZIP(update: Boolean, url: String): Flow<ResultProgress<String>> = flow {
        if (!Preferences.loadString("QEMU_INSTALLED").toBoolean() || update) {
            // Wipe previous installation if available
            FileManager.getFile("qemu").listFiles().forEach { it.delete() }
            // Install fresh QEMU
            val file = FileManager.downloadFile(url, "qemu-portable.zip")
            file.collect { resultProgress ->
                if (resultProgress.result != null) {
                    resultProgress.result.onSuccess { zipFile ->
                        val zip = ZipFile(zipFile)
                        try {
                            logger.debug { "Extracting ${zipFile.name}" }
                            zip.extractAll(FileManager.getFile("qemu").absolutePath)
                            Preferences.saveString("QEMU_INSTALLED", "true")
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
                    emit(ResultProgress.proceed(resultProgress.percentage, resultProgress.message))
                }
            }
        } else {
            logger.debug { "Already installed QEMU" }
            emit(ResultProgress.success(getString(Res.string.successful_installation, "QEMU")))
        }
    }

    fun downloadQEMU(update: Boolean = false): Flow<ResultProgress<String>> {
        logger.debug { "Downloading QEMU" }
        return when (getOS()) {
            OS.WINDOWS -> downloadQEMUWindows(update)
            OS.LINUX -> downloadQEMULinux(update)
            null -> flow {
                emit(ResultProgress.failure(UnsupportedOSException()))
            }
        }
    }

    fun downloadAlpineDisk(): Flow<ResultProgress<String>> = flow {
        logger.debug { "Downloading disk" }
        if (!FileManager.getFile("disk/alpine-linux.qcow2").exists()) {
            val url = "http://localhost:3000/alpine-linux.qcow2"
            val file = FileManager.downloadFile(url, "alpine-linux.qcow2", FileManager.getFile("disk"))
            file.collect { resultProgress ->
                if (resultProgress.result != null) {
                    resultProgress.result.onFailure {
                        emit(
                            ResultProgress.failure(
                                DownloadException(
                                    getString(
                                        Res.string.download_failed,
                                        it.localizedMessage
                                    )
                                )
                            )
                        )
                    }
                } else {
                    emit(ResultProgress.proceed(resultProgress.percentage, getString(Res.string.downloading)))
                }
            }
        } else {
            logger.debug { "Already installed disk" }
            emit(ResultProgress.success(getString(Res.string.successful_installation, "VM")))
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
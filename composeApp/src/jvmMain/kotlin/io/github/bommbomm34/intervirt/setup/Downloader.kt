package io.github.bommbomm34.intervirt.setup

import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.SUPPORTED_ARCHITECTURES
import io.github.bommbomm34.intervirt.data.FileManager
import io.github.bommbomm34.intervirt.data.OS
import io.github.bommbomm34.intervirt.data.Progress
import io.github.bommbomm34.intervirt.data.getOS
import io.github.bommbomm34.intervirt.exceptions.UnsupportedArchitectureException
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import org.jetbrains.compose.resources.getString

class Downloader(val fileManager: FileManager) {
    fun downloadQEMUWindows(update: Boolean = false): Flow<Progress> = flow {
        if (!fileManager.getFile("qemu/qemu-system-x86_64.exe").exists() || update) {
            // Wipe previous installation if available
            fileManager.getFile("qemu").listFiles().forEach { it.delete() }
            // Install fresh QEMU
            val url = "http://localhost:3000/qemu-portable.zip"
            val file = fileManager.downloadFile(url, "qemu-portable.zip")
            file.collect {
                if (it.result != null) {
                    it.result.onSuccess { zipFile ->
                        val zip = ZipFile(zipFile)
                        try {
                            logger.debug { "Extracting ${zipFile.name}" }
                            zip.extractAll(fileManager.getFile("qemu").absolutePath)
                            logger.debug { "Testing QEMU" }
                            emit(
                                Progress.success(
                                    getString(
                                        Res.string.download_succeeded,
                                        "QEMU"
                                    )
                                )
                            )
                        } catch (e: ZipException) {
                            logger.error { "Error occurred while extracting ${zipFile.name}: ${e.message}" }
                            emit(
                                Progress.error(
                                    getString(
                                        Res.string.error_while_zip_extraction,
                                        zipFile.name,
                                        e.localizedMessage
                                    )
                                )
                            )
                        }
                    }.onFailure {
                        emit(Progress.error(getString(Res.string.download_failed)))
                    }
                } else {
                    emit(Progress(it.percentage, "Downloading"))
                }
            }
        } else {
            logger.debug { "Already installed QEMU" }
            emit(Progress.success(getString(Res.string.successful_installation, "QEMU")))
        }
    }

    fun downloadQEMULinux(update: Boolean = false): Flow<Progress> = flow {
        if (!fileManager.getFile("qemu/qemu-system-x86_64").exists() || update) {
            // Wipe previous installation if available
            fileManager.getFile("qemu").listFiles().forEach { it.delete() }
            // Install fresh QEMU
//            val urlResult = getDownloadURL("http://localhost:3000/qemu-system-")
            val urlResult = getDownloadURL("http://localhost:3000/qemu-system-")
            urlResult.onSuccess { url ->
                logger.debug { "Determined download URL $url" }
                val file = fileManager.downloadFile(url, "qemu-system-x86_64", fileManager.getFile("qemu"))
                file.collect {
                    if (it.result != null) {
                        it.result.onSuccess { executable ->
                            executable.setExecutable(true)
                            emit(
                                Progress(
                                    1f,
                                    getString(Res.string.download_succeeded, "QEMU"),
                                    true
                                )
                            )
                        }.onFailure {
                            emit(Progress.error(getString(Res.string.download_failed, it.localizedMessage)))
                        }
                    } else {
                        emit(Progress(it.percentage, getString(Res.string.downloading)))
                    }
                }

            }.onFailure {
                logger.error { it.message }
                emit(Progress.error(getString(Res.string.arch_is_not_supported, System.getProperty("os.arch"))))
            }
        } else {
            logger.debug { "Already installed QEMU" }
            emit(Progress.success(getString(Res.string.successful_installation, "QEMU")))
        }
    }

    fun downloadQEMU(update: Boolean = false): Flow<Progress> {
        logger.debug { "Downloading QEMU" }
        return when (getOS()) {
            OS.WINDOWS -> downloadQEMUWindows(update)
            OS.LINUX -> downloadQEMULinux(update)
            null -> flow {
                emit(
                    Progress.error(getString(Res.string.os_is_not_supported, System.getProperty("os.name"), "QEMU"))
                )
            }
        }
    }

    fun downloadAlpineDisk(): Flow<Progress> = flow {
        logger.debug { "Downloading disk" }
        if (!fileManager.getFile("disk/alpine-linux.qcow2").exists()) {
            val url = "http://localhost:3000/alpine-linux.qcow2"
            val file = fileManager.downloadFile(url, "alpine-linux.qcow2", fileManager.getFile("disk"))
            file.collect {
                if (it.result != null) {
                    it.result.onFailure {
                        emit(Progress.error(getString(Res.string.download_failed, it.localizedMessage)))
                    }
                } else {
                    emit(Progress(it.percentage, "Downloading"))
                }
            }
        } else {
            logger.debug { "Already installed disk" }
            emit(Progress.success(getString(Res.string.successful_installation, "VM")))
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
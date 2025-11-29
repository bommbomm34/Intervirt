package io.github.bommbomm34.intervirt.setup

import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.SUPPORTED_ARCHITECTURES
import io.github.bommbomm34.intervirt.data.*
import io.github.bommbomm34.intervirt.exceptions.UnsupportedArchitectureException
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import org.jetbrains.compose.resources.getString

class Downloader(val fileManagement: FileManagement, val executor: Executor) {
    fun downloadQEMUWindows(update: Boolean = false): Flow<Progress> = flow {
        if (!fileManagement.getFile("qemu/qemu-system.exe").exists() || update){
            // Wipe previous installation if available
            fileManagement.getFile("qemu").listFiles().forEach { it.delete() }
            // Install fresh QEMU
            val url = "http://localhost:3000/qemu-portable.zip"
            val file = fileManagement.downloadFile(url, "qemu-portable.zip")
            file.onSuccess { zipFile ->
                val zip = ZipFile(zipFile)
                try {
                    logger.debug { "Extracting ${zipFile.name}" }
                    zip.extractAll(fileManagement.getFile("qemu").absolutePath)
                    logger.debug { "Testing QEMU" }
                    executor.runCommandOnHost("qemu", "./qemu-system.exe", "--version")
                        .collect {
                            if (it.message != null) logger.debug { it.message } else emit(Progress.success(getString(Res.string.download_succeeded, "QEMU")))
                        }
                } catch (e: ZipException) {
                    logger.error { "Error occurred while extracting ${zipFile.name}: ${e.message}" }
                    emit(Progress.error(getString(Res.string.error_while_zip_extraction, zipFile.name, e.localizedMessage)))
                }
            }.onFailure {
                emit(Progress.error(getString(Res.string.download_failed)))
            }
        } else {
            logger.debug { "Already installed QEMU" }
            emit(Progress.success(getString(Res.string.successful_installation, "QEMU")))
        }
    }

    fun downloadQEMULinux(update: Boolean = false): Flow<Progress> = flow {
        if (!fileManagement.getFile("qemu/qemu-system").exists() || update) {
            // Wipe previous installation if available
            fileManagement.getFile("qemu").listFiles().forEach { it.delete() }
            // Install fresh QEMU
            val urlResult = getDownloadURL("http://localhost:3000/qemu-system-")
            urlResult.onSuccess { url ->
                logger.debug { "Determined download URL $url" }
                val file = fileManagement.downloadFile(url, "qemu-system", fileManagement.getFile("qemu"))
                file.onSuccess { executable ->
                    executable.setExecutable(true)
                    executor.runCommandOnHost("qemu", "./qemu-system", "--version")
                        .collect { emit(Progress(1f, it.message ?: getString(Res.string.download_succeeded), true)) }
                }.onFailure {
                    emit(Progress.error(getString(Res.string.download_failed, it.localizedMessage)))
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

    fun downloadQEMU(update: Boolean = false): Flow<Progress> = when (getOS()) {
        OS.WINDOWS -> downloadQEMUWindows(update)
        OS.LINUX -> downloadQEMULinux(update)
        null -> flow {
            emit(
                Progress.error(getString(Res.string.os_is_not_supported, System.getProperty("os.name"), "QEMU"))
            )
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
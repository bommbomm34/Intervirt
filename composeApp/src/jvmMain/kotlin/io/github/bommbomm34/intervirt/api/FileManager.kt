package io.github.bommbomm34.intervirt.api

import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.download_failed
import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.OS
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.data.getOS
import io.github.bommbomm34.intervirt.exceptions.UnsupportedOsException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.call.*
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.asSink
import kotlinx.io.buffered
import org.jetbrains.compose.resources.getString
import java.io.File
import java.nio.file.Files

class FileManager(
    private val agentClient: AgentClient,
    preferences: Preferences
) {
    private val logger = KotlinLogging.logger {  }
    private val dataDir = preferences.DATA_DIR

    fun init() {
        dataDir.mkdir()
        dataDir.createFileInDirectory("qemu", true)
        dataDir.createFileInDirectory("disk", true)
        dataDir.createFileInDirectory("cache", true)
    }

    fun newFile(path: String): File {
        return dataDir.createFileInDirectory(path)
    }

    fun removeFile(path: String) {
        dataDir.createFileInDirectory(path)
    }

    fun getFile(name: String) = File(dataDir.absolutePath + File.separator + name)

    // Based on: https://ktor.io/docs/client-responses.html#streaming
    fun downloadFile(url: String, name: String, destination: File = getFile("cache")): Flow<ResultProgress<File>> =
        flow {
            logger.debug { "Downloading file $url as $name" }
            val bufferSize: Long = 1024 * 1024
            val file = File(destination.absolutePath + "/" + name)
            val stream = file.outputStream().asSink()

            client.prepareGet(url).execute { response ->
                if (response.status != HttpStatusCode.OK) {
                    emit(
                        ResultProgress.Companion.result(
                            Result.failure(
                                Exception(
                                    getString(Res.string.download_failed, response.status.description)
                                )
                            )
                        )
                    )
                } else {
                    val channel: ByteReadChannel = response.body()
                    val totalBytes = response.headers["Content-Length"]!!.toLong()
                    var count = 0L
                    stream.use {
                        while (!channel.exhausted()) {
                            val chunk = channel.readRemaining(bufferSize)
                            count += chunk.remaining

                            chunk.transferTo(stream)
                            logger.debug { "Downloaded $count bytes of $totalBytes bytes" }
                            emit(ResultProgress.Companion.proceed(count.toFloat() / totalBytes))
                        }
                    }
                    logger.debug { "Successfully downloaded $name" }
                    emit(ResultProgress.Companion.result(Result.success(file)))
                }
            }
        }

    fun uploadFile(url: String, file: File): Flow<ResultProgress<Unit>> = flow {
        client.post(url) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "file",
                            InputProvider { file.inputStream().asInput().buffered() },
                        )
                    },
                    boundary = "FileUploadBoundary"
                )
            )
            onUpload { bytesSentTotal, contentLength ->
                val progress = contentLength?.let { bytesSentTotal / it.toFloat() }
                emit(ResultProgress.Companion.proceed(progress ?: 0f))
            }
        }
    }

    fun getQemuFile(): File {
        return when (getOS()) {
            OS.WINDOWS -> getFile("qemu/qemu-system-x86_64")
            OS.LINUX -> getFile("qemu/usr/local/bin/qemu-system-x86_64")
            null -> throw UnsupportedOsException()
        }
    }

    fun getAlpineDisk(): File = getFile("disk/alpine-linux.qcow2")

    suspend fun pullFile(device: Device.Computer, path: String, destFile: PlatformFile): Result<Unit> {
        val res = agentClient.downloadFile(device.id, path, this)
        val file = res.getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.IO) {
            try {
                Files.move(file.toPath(), destFile.file.toPath())
                return@withContext Result.success(Unit)
            } catch (e: IOException){
                return@withContext Result.failure(e)
            }
        }
    }

    fun pushFile(device: Device.Computer, path: String, platformFile: PlatformFile) = agentClient.uploadFile(device.id, platformFile.file, path, this)
}

fun File.createFileInDirectory(name: String, directory: Boolean = false): File {
    if (!isDirectory) error("File $absolutePath must be a directory!")
    val file = File(absolutePath + File.separator + name)
    if (file.exists()) return file
    return file.apply { if (directory) mkdir() else createNewFile() }
}
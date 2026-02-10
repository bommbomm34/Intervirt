package io.github.bommbomm34.intervirt.api

import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.download_failed
import intervirt.composeapp.generated.resources.error_while_zip_extraction
import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.data.*
import io.github.bommbomm34.intervirt.exceptions.UnsupportedOsException
import io.github.bommbomm34.intervirt.exceptions.ZipExtractionException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.io.asSink
import kotlinx.io.buffered
import net.lingala.zip4j.ZipFile
import org.jetbrains.compose.resources.getString
import java.io.File
import java.util.zip.ZipException

class FileManager(appEnv: AppEnv) {
    private val logger = KotlinLogging.logger { }
    private val dataDir = appEnv.dataDir

    suspend fun init() = withContext(Dispatchers.IO) {
        dataDir.mkdir()
        dataDir.createFileInDirectory("qemu", true)
        dataDir.createFileInDirectory("disk", true)
        dataDir.createFileInDirectory("cache", true)
    }

    suspend fun newFile(path: String) = withContext(Dispatchers.IO) {
        dataDir.createFileInDirectory(path)
    }

    suspend fun removeFile(path: String) = withContext(Dispatchers.IO) {
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
                        ResultProgress.result(
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
                            emit(ResultProgress.proceed(count.toFloat() / totalBytes))
                        }
                    }
                    logger.debug { "Successfully downloaded $name" }
                    emit(ResultProgress.result(Result.success(file)))
                }
            }
        }.flowOn(Dispatchers.IO)

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
                emit(ResultProgress.proceed(progress ?: 0f))
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

    suspend fun extractZip(file: File, destination: File) = withContext(Dispatchers.IO) {
        try {
            logger.debug { "Extracting ${file.name}" }
            val zip = ZipFile(file)
            zip.extractAll(destination.absolutePath)
            Result.success(Unit)
        } catch (e: ZipException) {
            logger.error { "Error occurred while extracting ${file.name}: ${e.message}" }
            Result.failure(
                ZipExtractionException(
                    getString(
                        Res.string.error_while_zip_extraction,
                        file.name,
                        e.localizedMessage
                    )
                )
            )
        }
    }
}

fun File.createFileInDirectory(name: String, directory: Boolean = false): File {
    if (!isDirectory) error("File $absolutePath must be a directory!")
    val file = File(absolutePath + File.separator + name)
    if (file.exists()) return file
    return file.apply { if (directory) mkdir() else createNewFile() }
}
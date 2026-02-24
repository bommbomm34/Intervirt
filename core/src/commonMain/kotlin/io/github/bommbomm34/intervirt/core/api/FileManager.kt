package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.OS
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.getOS
import io.github.bommbomm34.intervirt.core.exceptions.ZipExtractionException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.io.asSink
import net.lingala.zip4j.ZipFile
import java.io.File
import java.util.zip.ZipException

class FileManager(
    appEnv: AppEnv,
    private val client: HttpClient,
) {
    private val logger = KotlinLogging.logger { }
    private val dataDir = appEnv.DATA_DIR

    suspend fun init() = withContext(Dispatchers.IO) {
        logger.debug { "Initializing FileManager" }
        dataDir.mkdir()
        dataDir.createFileInDirectory("qemu", true)
        dataDir.createFileInDirectory("disk", true)
        dataDir.createFileInDirectory("cache", true)
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
                                    "Download failed ${response.status.description}",
                                ),
                            ),
                        ),
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

    fun getQemuFile(): File {
        return when (getOS()) {
            OS.WINDOWS -> getFile("qemu/qemu-system-x86_64")
            OS.LINUX -> getFile("qemu/usr/local/bin/qemu-system-x86_64")
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
            Result.failure(ZipExtractionException(file.name, e.localizedMessage))
        }
    }
}

fun File.createFileInDirectory(name: String, directory: Boolean = false): File {
    if (!isDirectory) error("File $absolutePath must be a directory!")
    val file = File(absolutePath + File.separator + name)
    if (file.exists()) return file
    return file.apply { if (directory) mkdir() else createNewFile() }
}
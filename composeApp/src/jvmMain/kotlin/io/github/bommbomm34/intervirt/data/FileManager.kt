package io.github.bommbomm34.intervirt.data

import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.download_failed
import io.github.bommbomm34.intervirt.DATA_DIR
import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.exceptions.UnsupportedOSException
import io.github.bommbomm34.intervirt.logger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.asSink
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import java.io.File
import java.nio.file.Files

object FileManager {

    fun init() {
        DATA_DIR.mkdir()
        DATA_DIR.createFileInDirectory("qemu", true)
        DATA_DIR.createFileInDirectory("disk", true)
        DATA_DIR.createFileInDirectory("cache", true)
    }

    fun newFile(path: String): File {
        return DATA_DIR.createFileInDirectory(path)
    }

    fun removeFile(path: String) {
        DATA_DIR.createFileInDirectory(path)
    }

    fun getFile(name: String) = File(DATA_DIR.absolutePath + File.separator + name)

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
        }

    fun getQEMUFile(): File {
        return when (getOS()){
            OS.WINDOWS -> getFile("qemu/qemu-system-x86_64")
            OS.LINUX -> getFile("qemu/usr/local/bin/qemu-system-x86_64")
            null -> throw UnsupportedOSException()
        }
    }
}

fun File.createFileInDirectory(name: String, directory: Boolean = false): File {
    if (!isDirectory) error("File $absolutePath must be a directory!")
    logger.debug { "Creating directory $name" }
    val file = File(absolutePath + File.separator + name)
    if (file.exists()) return file
    return file.apply { if (directory) mkdir() else createNewFile() }
}
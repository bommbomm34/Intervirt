package io.github.bommbomm34.intervirt.data

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.download_failed
import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.guestSession
import io.github.bommbomm34.intervirt.logger
import io.github.bommbomm34.intervirt.preferences
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.asSink
import org.jetbrains.compose.resources.getString
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.sequences.forEach

class FileManagement(val dataDir: File) {
    init {
        preferences.saveString("dataPath", dataDir.absolutePath + File.separator)
        dataDir.createFileInDirectory("qemu", true)
        dataDir.createFileInDirectory("disk", true)
        dataDir.createFileInDirectory("cache", true)
    }

    fun newFile(path: String, directory: Boolean = false): File {
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
        val linuxFile = getFile("qemu/qemu-system-x86_64")
        val windowsFile = getFile("qemu/qemu-system-x86_64.exe")
        return if (linuxFile.exists()) linuxFile else windowsFile
    }

    fun sendFileToGuest(file: File, destinationFolder: String){
        logger.info { "Sending file ${file.name} to guest" }
        if (!guestSession.isConnected) guestSession.connect()
        val channel = guestSession.openChannel("sftp") as ChannelSftp
        channel.connect()
        channel.put(file.absolutePath, destinationFolder, ChannelSftp.OVERWRITE)
        channel.disconnect()
    }
}

fun File.createFileInDirectory(name: String, directory: Boolean = false): File {
    if (!isDirectory) error("File $absolutePath must be a directory!")
    logger.debug { "Creating directory $name" }
    val file = File(absolutePath + File.separator + name)
    if (file.exists()) return file
    return file.apply { if (directory) mkdir() else createNewFile() }
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.left
import arrow.core.right
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.OS
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.getOS
import io.github.bommbomm34.intervirt.core.exceptions.ZipExtractionException
import io.github.bommbomm34.intervirt.core.util.ext.createFile
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.vinceglb.filekit.*
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
import net.lingala.zip4j.ZipFile
import java.util.zip.ZipException

class FileManager(
    appEnv: AppEnv,
    private val client: HttpClient,
) {
    private val logger = appEnv.getLogger(FileManager::class)
    private val dataDir = appEnv.DATA_DIR

    suspend fun init() {
        logger.debug { "Initializing FileManager" }
        dataDir.createDirectories()
        dataDir.createFileInDirectory("qemu", true)
        dataDir.createFileInDirectory("disk", true)
        dataDir.createFileInDirectory("cache", true)
        logger.debug { "Initialized FileManager" }
    }

    fun getFile(name: String) = dataDir / name

    // Based on: https://ktor.io/docs/client-responses.html#streaming
    fun downloadFile(url: String, name: String, destination: PlatformFile = getFile("cache")): Flow<ResultProgress<PlatformFile>> =
        flow {
            logger.debug { "Downloading file $url as $name" }
            val bufferSize: Long = 1024 * 1024
            val file = destination / name
            val stream = file.sink()

            client.prepareGet(url).execute { response ->
                if (response.status != HttpStatusCode.OK) {
                    logger.error { "Failed to download file '$url': ${response.status.description}" }
                    emit(
                        ResultProgress.failure(Failure.Download(response.status.description)),
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
                    logger.info { "Successfully downloaded $name" }
                    emit(ResultProgress.success(file))
                }
            }
        }.flowOn(Dispatchers.IO)

    fun getQemuFile(): PlatformFile {
        return when (getOS()) {
            OS.WINDOWS -> getFile("qemu/qemu-system-x86_64")
            OS.LINUX -> getFile("qemu/usr/local/bin/qemu-system-x86_64")
        }
    }

    fun getAlpineDisk(): PlatformFile = getFile("disk/alpine-linux.qcow2")

    suspend fun extractZip(file: PlatformFile, destination: PlatformFile) = withContext(Dispatchers.IO) {
        try {
            logger.debug { "Extracting ${file.name}" }
            val zip = ZipFile(file.file)
            zip.extractAll(destination.absolutePath())
            logger.info { "Extracted zip ${file.name}" }
            Unit.right()
        } catch (e: ZipException) {
            logger.error { "Error occurred while extracting ${file.name}: ${e.message}" }
            Failure.ZipExtraction(file.name, e.message).left()
        }
    }
}

private suspend fun PlatformFile.createFileInDirectory(name: String, directory: Boolean = false): PlatformFile {
    if (!isDirectory()) error("File ${absolutePath()} must be a directory!")
    val file = this / name
    if (file.exists()) return file
    return file.apply { if (directory) createDirectories() else createFile() }
}

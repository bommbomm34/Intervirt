package io.github.bommbomm34.intervirt.api.impl

import io.github.bommbomm34.intervirt.api.RemoteContainerSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream

class VirtualRemoteContainerSession(
    override val id: String
) : RemoteContainerSession {
    private val process: Process
    private val inputStream: BufferedInputStream
    private val outputStream: BufferedOutputStream

    init {
        val builder = ProcessBuilder("/bin/bash")
        builder.redirectErrorStream()
        process = builder.start()
        inputStream = process.inputStream.buffered()
        outputStream = process.outputStream.buffered()
    }

    override suspend fun write(bytes: ByteArray): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            outputStream.write(bytes)
        }
    }

    override fun read(length: Int): ByteArray = inputStream.readNBytes(length)

    override suspend fun close() = withContext(Dispatchers.IO) {
        inputStream.close()
        outputStream.close()
        process.destroy()
    }

    override fun isConnected(): Boolean = process.isAlive
}
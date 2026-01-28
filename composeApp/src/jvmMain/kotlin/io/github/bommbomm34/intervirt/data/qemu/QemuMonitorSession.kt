package io.github.bommbomm34.intervirt.data.qemu

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class QemuMonitorSession(
    private val selector: ActorSelectorManager,
    private val socket: Socket
) {
    val socketLock = Mutex()
    private val readChannel = socket.openReadChannel()
    private val writeChannel = socket.openWriteChannel(autoFlush = true)

    suspend inline fun <T> withLock(action: QemuMonitorSession.() -> T): T = socketLock.withLock { action() }

    suspend fun writeLine(line: String) {
        if (!writeChannel.isClosedForWrite){
            writeChannel.writeString(line + "\n")
        }
    }

    suspend fun readLine(): String? {
        return if (!readChannel.isClosedForRead){
            readChannel.readLine()
        } else null
    }

    suspend fun close() = withContext(Dispatchers.IO) {
        socket.close()
        selector.close()
    }
}
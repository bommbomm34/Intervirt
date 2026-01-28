package io.github.bommbomm34.intervirt.data

import androidx.compose.ui.util.fastSumBy
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class RemoteContainerSession(
    val id: String,
    private val websocket: DefaultClientWebSocketSession,
) {
    // TODO: Fill byteStack
    private val byteStack = mutableListOf<ByteArray>()
    private val byteStackLock = Mutex()

    suspend fun write(bytes: ByteArray): Result<Unit> = runCatching {
        websocket.send(bytes)
    }

    suspend fun read(): ByteArray {
        byteStackLock.withLock {
            val byteArray = byteStack.flatMap { it.toList() }.toByteArray()
            byteStack.clear()
            return byteArray
        }
    }

    suspend fun close() = websocket.close()

    fun isConnected(): Boolean = websocket.isActive
}
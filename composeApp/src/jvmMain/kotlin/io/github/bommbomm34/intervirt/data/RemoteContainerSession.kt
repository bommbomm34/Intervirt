package io.github.bommbomm34.intervirt.data

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.isActive

data class RemoteContainerSession(
    val id: String,
    private val websocket: DefaultClientWebSocketSession
) {

    suspend fun write(bytes: ByteArray): Result<Unit> = runCatching {
        websocket.send(bytes)
    }

    fun read(length: Int): ByteArray {
        val byteArray = websocket.incoming
            .receive(length)
            .flatMap { it.toList() }
            .toByteArray()
        return byteArray
    }

    suspend fun close() = websocket.close()
    fun isConnected(): Boolean = websocket.isActive

    private fun ReceiveChannel<Frame>.receive(length: Int): List<ByteArray> {
        val list = mutableListOf<ByteArray>()
        var lengthOfBytes = 0
        while (true) {
            val frame = tryReceive().getOrElse { return list }
            val bytes = (frame as Frame.Binary).readBytes()
            lengthOfBytes += bytes.size
            if (lengthOfBytes < length) list.add(bytes) else return list
        }
    }
}
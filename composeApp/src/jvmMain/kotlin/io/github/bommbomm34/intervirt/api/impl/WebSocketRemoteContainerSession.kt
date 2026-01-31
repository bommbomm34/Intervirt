package io.github.bommbomm34.intervirt.api.impl

import io.github.bommbomm34.intervirt.api.RemoteContainerSession
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.launch

class WebSocketRemoteContainerSession(
    override val id: String,
    private val incoming: ReceiveChannel<ByteArray>,
    private val outgoing: SendChannel<ByteArray>,
    private val onClose: suspend () -> Unit = {}
) : RemoteContainerSession {

    override suspend fun write(bytes: ByteArray): Result<Unit> = runCatching {
        outgoing.send(bytes)
    }

    override fun read(length: Int): ByteArray {
        val byteArray = incoming
            .receive(length)
            .flatMap { it.toList() }
            .toByteArray()
        return byteArray
    }

    override suspend fun close() = onClose()

    // TODO: Review Delicate API
    @OptIn(DelicateCoroutinesApi::class)
    override fun isConnected(): Boolean = !incoming.isClosedForReceive && !outgoing.isClosedForSend

    private fun ReceiveChannel<ByteArray>.receive(length: Int): List<ByteArray> {
        val list = mutableListOf<ByteArray>()
        var lengthOfBytes = 0
        while (true) {
            val bytes = tryReceive().getOrElse { return list }
            lengthOfBytes += bytes.size
            if (lengthOfBytes < length) list.add(bytes) else return list
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.toByteArrayChannel(channel: ReceiveChannel<Frame>): ReceiveChannel<ByteArray> = produce {
    for (frame in channel) {
        send(frame.readBytes())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.toByteArrayChannel(channel: SendChannel<Frame>): SendChannel<ByteArray> {
    val input = Channel<ByteArray>(Channel.BUFFERED)

    launch {
        for (bytes in input) {
            channel.send(Frame.Binary(fin = true, data = bytes))
        }
    }

    return input
}
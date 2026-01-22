package io.github.bommbomm34.intervirt.data

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession

data class RemoteContainerSession(
    val id: String, // Container id
    val websocket: DefaultClientWebSocketSession,
    val inputStream: ContainerInputStream,
    val outputStream: ContainerOutputStream
)

package io.github.bommbomm34.intervirt.api

interface RemoteContainerSession {
    val id: String

    suspend fun write(bytes: ByteArray): Result<Unit>

    fun read(length: Int): ByteArray

    suspend fun close()

    fun isConnected(): Boolean
}
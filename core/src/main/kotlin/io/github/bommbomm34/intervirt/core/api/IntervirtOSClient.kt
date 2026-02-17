package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.intervirtos.store.IntervirtOSStore
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable

class IntervirtOSClient(private val client: Client) : AsyncCloseable {
    private val managers = mutableListOf<AsyncCloseable>()

    data class Client(
        val computer: Device.Computer,
        val ioClient: ContainerIOClient,
        val store: IntervirtOSStore = IntervirtOSStore(ioClient),
        val serviceManager: SystemServiceManager = SystemServiceManager(ioClient)
    )

    suspend fun init(): Result<Unit> = runSuspendingCatching {
        client.store.init().getOrThrow()
    }

    fun getClient(
        manager: AsyncCloseable? = null
    ): Client {
        manager?.let { managers.add(it) }
        return client
    }

    override suspend fun close(): Result<Unit> = runSuspendingCatching {
        managers.forEach { it.close().getOrThrow() }
    }
}
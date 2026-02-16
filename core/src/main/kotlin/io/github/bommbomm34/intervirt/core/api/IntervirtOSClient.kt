package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.intervirtos.IntervirtOSStore
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

    fun getClient(
        manager: AsyncCloseable? = null
    ): Client {
        manager?.let { managers.add(it) }
        return Client(client.computer, client.ioClient, client.store, client.serviceManager)
    }

    override suspend fun close(): Result<Unit> = runSuspendingCatching {
        managers.forEach { it.close().getOrThrow() }
    }
}
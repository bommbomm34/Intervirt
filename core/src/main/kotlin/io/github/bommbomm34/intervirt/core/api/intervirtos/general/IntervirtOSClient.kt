package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.SystemServiceManager
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable

class IntervirtOSClient(private val client: Client) : AsyncCloseable {
    private val managers = mutableListOf<AsyncCloseable>()

    data class Client(
        val computer: Device.Computer,
        val ioClient: ContainerIOClient,
        val store: IntervirtOSStore = IntervirtOSStore(ioClient),
        val serviceManager: SystemServiceManager = SystemServiceManager(ioClient),
        val docker: DockerManager,
    )

    suspend fun init(): Result<Unit> = runSuspendingCatching {
        client.store.init().getOrThrow()
        client.docker.init().getOrThrow()
    }

    fun getClient(
        manager: AsyncCloseable? = null,
    ): Client {
        manager?.let { managers.add(it) }
        return client
    }

    override suspend fun close(): Result<Unit> = runSuspendingCatching {
        managers.forEach { it.close().getOrThrow() }
        // Don't close ioClient because it's externally managed
        client.docker.close().getOrThrow()
    }
}
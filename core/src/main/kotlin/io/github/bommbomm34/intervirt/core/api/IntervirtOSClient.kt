package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable

class IntervirtOSClient (
    private val computer: Device.Computer,
    private val ioClient: ContainerIOClient,
    private val serviceManager: SystemServiceManager = SystemServiceManager(ioClient)
) : AsyncCloseable {
    private val managers = mutableListOf<AsyncCloseable>()

    data class Client(
        val computer: Device.Computer,
        val ioClient: ContainerIOClient,
        val serviceManager: SystemServiceManager
    )

    fun getClient(
        manager: AsyncCloseable? = null
    ): Client {
        manager?.let { managers.add(it) }
        return Client(computer, ioClient, serviceManager)
    }

    override suspend fun close(): Result<Unit> = runSuspendingCatching {
        managers.forEach { it.close().getOrThrow() }
    }
}
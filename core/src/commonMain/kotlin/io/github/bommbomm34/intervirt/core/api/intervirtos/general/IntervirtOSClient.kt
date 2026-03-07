/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.SystemServiceManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable

class IntervirtOSClient(private val client: Client) : AsyncCloseable {
    private val managers = mutableListOf<AsyncCloseable>()

    data class Client(
        private val appEnv: AppEnv,
        val computer: Device.Computer,
        val ioClient: ContainerIOClient,
        val docker: DockerManager,
        val store: IntervirtOSStore = IntervirtOSStore(appEnv, ioClient),
        val serviceManager: SystemServiceManager = SystemServiceManager(appEnv, ioClient),
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
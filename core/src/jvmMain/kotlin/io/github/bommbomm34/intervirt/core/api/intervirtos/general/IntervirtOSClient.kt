/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import arrow.core.raise.context.bind
import arrow.core.raise.either
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.SystemServiceManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.Device
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

    suspend fun init(): AppResult<Unit> = either {
        client.store.init().bind()
        client.docker.init().bind()
    }

    fun getClient(
        manager: AsyncCloseable? = null,
    ): Client {
        manager?.let { managers.add(it) }
        return client
    }

    override suspend fun close(): AppResult<Unit> = either {
        managers.forEach { it.close().bind() }
        // Don't close ioClient because it's externally managed
        client.docker.close().bind()
    }
}

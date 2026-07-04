/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.api.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.SystemServiceManager
import io.github.bommbomm34.intervirt.core.data.env.AppEnv

import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable

class IntervirtOSClient(private val client: Client) : AsyncCloseable {
    private val managers = mutableListOf<AsyncCloseable>()

    data class Client(
        private val envHolder: AppEnvHolder,
        val computer: Device.Computer,
        val ioClient: ContainerIOClient,
        val docker: DockerManager,
        val store: IntervirtOSStore = IntervirtOSStore(envHolder, ioClient),
        val serviceManager: SystemServiceManager = SystemServiceManager(envHolder, ioClient),
    )

    context(_: Raise<Failure>)
    suspend fun init() {
        client.store.init()
        client.docker.init()
    }

    fun getClient(
        manager: AsyncCloseable? = null,
    ): Client {
        manager?.let { managers.add(it) }
        return client
    }

    context(_: Raise<Failure>)
    override suspend fun close() {
        managers.forEach { it.close() }
        // Don't close ioClient because it's externally managed
        client.docker.close()
    }
}

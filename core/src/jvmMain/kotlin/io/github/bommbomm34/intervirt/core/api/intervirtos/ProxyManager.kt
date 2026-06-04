/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos

import arrow.core.right
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.getFreePort
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.ext.getLogger


class ProxyManager(
    appEnv: AppEnv,
    private val deviceManager: DeviceManager,
    osClient: IntervirtOSClient,
) : AsyncCloseable {
    private val client = osClient.getClient(this)
    private val logger = appEnv.getLogger(ProxyManager::class)
    private val computer = client.computer
    private val virtual = appEnv.VIRTUAL_CONTAINER_IO
    private var proxyUrl: Address? = null

    suspend fun getProxyUrl() = if (virtual) Address("127.0.0.1", 1080).right() else {
        val url = proxyUrl
        if (url != null) url.right() else {
            logger.debug { "Initializing proxy" }
            val port = getFreePort()
            logger.debug { "Chose free port $port" }
            deviceManager.addPortForwarding(
                device = computer,
                PortForwarding(
                    protocol = "tcp",
                    internalPort = 1080,
                    externalPort = port,
                    hidden = true,
                ),
            )
                .map { Address("127.0.0.1", port) }
                .onRight {
                    logger.debug { "Successfully initialized proxy: $it" }
                    proxyUrl = it
                }
        }
    }

    override suspend fun close() = if (virtual) Unit.right() else {
        val url = proxyUrl
        if (url == null) Unit.right() else {
            deviceManager.removePortForwarding(
                externalPort = url.port,
                protocol = "tcp",
            )
        }
    }
}

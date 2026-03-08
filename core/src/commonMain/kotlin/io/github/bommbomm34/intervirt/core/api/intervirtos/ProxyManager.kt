/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.getFreePort
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.getLogger


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

    suspend fun getProxyUrl() = if (virtual) Result.success(Address("127.0.0.1", 1080)) else {
        val url = proxyUrl
        if (url != null) Result.success(url) else {
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
                .onSuccess {
                    logger.debug { "Successfully initialized proxy: $it" }
                    proxyUrl = it
                }
        }
    }

    override suspend fun close() = if (virtual) Result.success(Unit) else {
        val url = proxyUrl
        if (url == null) Result.success(Unit) else {
            deviceManager.removePortForwarding(
                externalPort = url.port,
                protocol = "tcp",
            )
        }
    }
}
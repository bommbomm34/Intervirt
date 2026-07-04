/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos

import arrow.core.raise.context.Raise
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.getFreePort
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure

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
    private val virtual = appEnv.virtualContainerIO
    private var proxyUrl: Address? = null

    context(_: Raise<Failure>)
    suspend fun getProxyUrl() = if (virtual) Address("127.0.0.1", 1080) else {
        val url = proxyUrl
        if (url != null) url else {
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
            ).let {
                val address = Address("127.0.0.1", port)
                logger.debug { "Successfully initialized proxy: $address" }
                proxyUrl = address
                address
            }
        }
    }

    context(_: Raise<Failure>)
    override suspend fun close() = if (virtual) Unit else {
        val url = proxyUrl
        if (url == null) Unit else {
            deviceManager.removePortForwarding(
                externalPort = url.port,
                protocol = "tcp",
            )
        }
    }
}

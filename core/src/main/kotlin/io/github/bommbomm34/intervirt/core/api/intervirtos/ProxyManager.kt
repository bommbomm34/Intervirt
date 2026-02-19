package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.getFreePort
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.oshai.kotlinlogging.KotlinLogging

class ProxyManager(
    appEnv: AppEnv,
    private val deviceManager: DeviceManager,
    osClient: IntervirtOSClient,
) : AsyncCloseable {
    private val client = osClient.getClient(this)
    private val logger = KotlinLogging.logger { }
    private val computer = client.computer
    private val virtual = appEnv.virtualContainerIO
    private var proxyUrl: Address? = null

    suspend fun getProxyUrl() = if (virtual) Result.success(Address("127.0.0.1", 1080)) else {
        val url = proxyUrl
        if (url != null) Result.success(url) else {
            logger.debug { "Initializing proxy" }
            val port = getFreePort()
            logger.debug { "Chose free port $port" }
            deviceManager.addPortForwarding(
                device = computer,
                internalPort = 1080,
                externalPort = port,
                protocol = "tcp",
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
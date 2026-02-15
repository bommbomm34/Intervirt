package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.getFreePort
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv

class BrowserManager(
    appEnv: AppEnv,
    bundle: ContainerClientBundle
) {
    private val computer = bundle.computer
    private val virtual = appEnv.virtualContainerIO

    suspend fun getProxyUrl(
        deviceManager: DeviceManager
    ) = if (virtual) Result.success(Address("127.0.0.1", 1080)) else {
        val port = getFreePort()
        deviceManager.addPortForwarding(
            device = computer,
            internalPort = 1080,
            externalPort = port,
            protocol = "tcp"
        ).map { Address("127.0.0.1", port) }
    }
}
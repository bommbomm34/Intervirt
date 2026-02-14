package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.data.Address

class BrowserManager(
    bundle: io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
) {
    private val computer = bundle.computer
    suspend fun getProxyUrl(
        deviceManager: io.github.bommbomm34.intervirt.core.api.DeviceManager
    ): Result<Address> {
        val port = _root_ide_package_.io.github.bommbomm34.intervirt.core.api.getFreePort()
        return deviceManager.addPortForwarding(
            device = computer,
            internalPort = 1080,
            externalPort = port,
            protocol = "tcp"
        ).map { Address("127.0.0.1", port) }
    }
}
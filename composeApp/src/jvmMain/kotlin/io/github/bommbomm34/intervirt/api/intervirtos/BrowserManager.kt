package io.github.bommbomm34.intervirt.api.intervirtos

import io.github.bommbomm34.intervirt.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.api.getFreePort
import io.github.bommbomm34.intervirt.data.Address

class BrowserManager(
    bundle: ContainerClientBundle
) {
    private val computer = bundle.computer
    suspend fun getProxyUrl(
        deviceManager: DeviceManager
    ): Result<Address> {
        val port = getFreePort()
        return deviceManager.addPortForwarding(
            device = computer,
            internalPort = 1080,
            externalPort = port,
            protocol = "tcp"
        ).map { Address("127.0.0.1", port) }
    }
}
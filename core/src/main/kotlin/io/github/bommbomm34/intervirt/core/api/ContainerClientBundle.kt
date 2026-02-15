package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.Device

data class ContainerClientBundle (
    val computer: Device.Computer,
    val ioClient: ContainerIOClient
){
    val serviceManager = SystemServiceManager(ioClient)
}
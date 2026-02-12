package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.Device

data class ContainerClientBundle (
    val computer: Device.Computer,
    val ioClient: ContainerIOClient
){
    val serviceManager = SystemServiceManager(ioClient)
}
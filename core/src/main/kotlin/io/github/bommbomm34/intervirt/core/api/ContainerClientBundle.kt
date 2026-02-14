package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.Device

data class ContainerClientBundle (
    val computer: Device.Computer,
    val ioClient: io.github.bommbomm34.intervirt.core.api.ContainerIOClient
){
    val serviceManager = _root_ide_package_.io.github.bommbomm34.intervirt.core.api.SystemServiceManager(ioClient)
}
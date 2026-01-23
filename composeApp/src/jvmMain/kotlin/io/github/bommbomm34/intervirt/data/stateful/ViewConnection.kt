package io.github.bommbomm34.intervirt.data.stateful

import io.github.bommbomm34.intervirt.data.DeviceConnection

data class ViewConnection(
    val device1: ViewDevice,
    val device2: ViewDevice
){
    fun containsDevice(device: ViewDevice) = device1.id == device.id || device2.id == device.id
}
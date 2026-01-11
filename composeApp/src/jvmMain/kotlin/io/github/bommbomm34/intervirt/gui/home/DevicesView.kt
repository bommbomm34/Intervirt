package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.*
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.DeviceConnection
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.AddDeviceButton
import io.github.bommbomm34.intervirt.gui.components.device.settings.DeviceSettings

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DevicesView() {
    var selectedDevice: ViewDevice? by remember { mutableStateOf(null) }
    var deviceSettingsVisible by remember { mutableStateOf(false) }
    AlignedBox(Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize(0.5f)
                .onPointerEvent(PointerEventType.Scroll) {
                    val delta = it.changes.first().scrollDelta.y * -ZOOM_SPEED
                    if (isCtrlPressed && devicesViewZoom + delta > 0.1f) devicesViewZoom += delta
                }
        ) {
            scale(devicesViewZoom) {
                statefulConf.connections.forEach { drawConnection(it) }
            }
        }
    }
    statefulConf.devices.forEach { device ->
        DeviceView(
            device = device,
            onSelectDevice = {
                selectedDevice = it
                deviceSettingsVisible = true
            }
        )
    }
    AnimatedVisibility(deviceSettingsVisible){
        selectedDevice?.let {
            Column (
                Modifier.padding(16.dp)
            ){
                DeviceSettings(
                    device = it
                ) { deviceSettingsVisible = false }
            }
        }

    }
    AlignedBox(Alignment.BottomEnd) {
        AddDeviceButton()
    }
}

fun DrawScope.drawConnection(connection: DeviceConnection) {
    drawLine(
        start = Offset(connection.device1.x.toFloat(), connection.device1.y.toFloat()),
        end = Offset(connection.device2.x.toFloat(), connection.device2.y.toFloat()),
        color = Color(DEVICE_CONNECTION_COLOR),
        strokeWidth = CONNECTION_STROKE_WIDTH
    )
}

fun Offset.isOn(device: Device, image: ImageBitmap): Boolean = x in device.x.toFloat()..(device.x.toFloat() + image.width) &&
        y in device.y.toFloat()..(device.y.toFloat() + image.height)
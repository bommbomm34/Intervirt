package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.DevicesPc
import compose.icons.tablericons.Switch
import io.github.bommbomm34.intervirt.*
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.DeviceConnection

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DevicesView() {
    var selectedDevice: Device? by remember { mutableStateOf(null) }
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
                configuration.connections.forEach { drawConnection(it) }
            }
        }
    }
    configuration.devices.forEach { device ->
        DeviceView(
            device = device,
            onSelectDevice = { selectedDevice = it }
        )
    }
    AnimatedVisibility(selectedDevice != null){
        Column (horizontalAlignment = Alignment.End) {
            DeviceSettings(
                device = selectedDevice!!
            ){ selectedDevice = null }
        }
    }
}

fun DrawScope.drawDevice(device: Device, image: ImageBitmap) {
    drawImage(
        image = image,
        topLeft = Offset(device.x.toFloat(), device.y.toFloat())
    )
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
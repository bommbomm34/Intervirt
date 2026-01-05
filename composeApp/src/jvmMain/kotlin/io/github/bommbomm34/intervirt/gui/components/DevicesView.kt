package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.onClick
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.computer
import intervirt.composeapp.generated.resources.switch
import io.github.bommbomm34.intervirt.*
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.DeviceConnection
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.imageResource

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DevicesView() {
    val computerImage = imageResource(Res.drawable.computer)
    val switchImage = imageResource(Res.drawable.switch)
    val getImage: (Device) -> ImageBitmap = {
        when (it) {
            is Device.Switch -> switchImage
            is Device.Computer -> computerImage
        }
    }
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
        Image(
            bitmap = getImage(device),
            contentDescription = device.name,
            modifier = Modifier
                .offset(device.x.dp, device.y.dp)
                .onClick { selectedDevice = device }
                .onDrag {
                    DeviceManager.setPosition(device, Offset(device.x, device.y) + it)
                }
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
        topLeft = Offset(device.x, device.y)
    )
}

fun DrawScope.drawConnection(connection: DeviceConnection) {
    drawLine(
        start = Offset(connection.device1.x, connection.device1.y),
        end = Offset(connection.device2.x, connection.device2.y),
        color = Color(DEVICE_CONNECTION_COLOR),
        strokeWidth = CONNECTION_STROKE_WIDTH
    )
}

fun Offset.isOn(device: Device, image: ImageBitmap): Boolean = x in device.x..(device.x + image.width) &&
        y in device.y..(device.y + image.height)
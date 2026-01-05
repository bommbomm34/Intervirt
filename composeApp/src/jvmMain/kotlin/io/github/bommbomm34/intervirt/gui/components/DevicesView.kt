package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
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
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.computer
import intervirt.composeapp.generated.resources.switch
import io.github.bommbomm34.intervirt.*
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.DeviceConnection
import org.jetbrains.compose.resources.imageResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DevicesView() {
    val computerImage = imageResource(Res.drawable.computer)
    val switchImage = imageResource(Res.drawable.switch)
    var isMousePressed by remember { mutableStateOf(false) }
    var statefulConf by remember { mutableStateOf(configuration) }
    val getImage: (Device) -> ImageBitmap = {
        when (it) {
            is Device.Switch -> switchImage
            is Device.Computer -> computerImage
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            if (statefulConf != configuration) statefulConf = configuration
        }
    }
    AlignedBox(Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize(0.5f)
                .onPointerEvent(PointerEventType.Scroll) {
                    val delta = it.changes.first().scrollDelta.y * -ZOOM_SPEED
                    if (isCtrlPressed && devicesViewZoom + delta > 0.1f) devicesViewZoom += delta
                }
                .onPointerEvent(PointerEventType.Move) { event ->
                    val position = event.changes.first().position
                    val hoveredDevice = configuration.devices.firstOrNull { position.isOn(it, getImage(it)) }
                    if (isMousePressed && hoveredDevice != null) hoveredDevice.x += 10
                }
                .onPointerEvent(PointerEventType.Enter) { isMousePressed = true }
                .onPointerEvent(PointerEventType.Exit) { isMousePressed = false }
        ) {
            scale(devicesViewZoom) {
                drawImage(
                    image = computerImage,
                    topLeft = Offset(size.width - computerImage.width / 2f, 0f - computerImage.height / 2f)
                )
                drawLine(
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    color = Color.Blue,
                    strokeWidth = CONNECTION_STROKE_WIDTH
                )
            }
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
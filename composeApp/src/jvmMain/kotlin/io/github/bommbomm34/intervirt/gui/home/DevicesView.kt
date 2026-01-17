package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
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
import io.github.bommbomm34.intervirt.*
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.AddDeviceButton
import io.github.bommbomm34.intervirt.gui.components.device.settings.DeviceSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DevicesView() {
    var selectedDevice: ViewDevice? by remember { mutableStateOf(null) }
    var deviceSettingsVisible by remember { mutableStateOf(false) }
    var drawingConnectionSource: ViewDevice? by remember { mutableStateOf(null) }
    var mousePosition by remember { mutableStateOf(Offset.Zero) }
    val scope = rememberCoroutineScope()
    AlignedBox(Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .onPointerEvent(PointerEventType.Scroll) {
                    val delta = it.changes.first().scrollDelta.y * -ZOOM_SPEED
                    if (isCtrlPressed && devicesViewZoom + delta > 0.1f) devicesViewZoom += delta
                }
                .onPointerEvent(PointerEventType.Press) {
                    mousePosition = it.changes.first().position
                }
        ) {
            scale(devicesViewZoom) {
//                drawingConnectionSource?.let {
//                    drawConnection(
//                        offset1 = it.offset,
//                        offset2 = mousePosition
//                    )
//                }
                statefulConf.connections.forEach {
                    drawConnection(
                        offset1 = it.device1.fittingOffset(),
                        offset2 = it.device2.fittingOffset()
                    )
                }
//                drawLine(
//                    start = statefulConf.connections[0].device1.fittingOffset(),
//                    end = statefulConf.connections[0].device2.fittingOffset(),
//                    color = Color.Blue,
//                    strokeWidth = CONNECTION_STROKE_WIDTH
//                )
            }
        }
    }
    statefulConf.devices.forEach { device ->
        DeviceView(
            device = device,
            onClickDevice = {
                if (selectedDevice != it || !deviceSettingsVisible) {
                    selectedDevice = it
                    deviceSettingsVisible = true
                } else deviceSettingsVisible = false
            },
            onSecondaryClick = {
                val copy = drawingConnectionSource
                if (copy != null) {
                    statefulConf.connections.add(copy connect it)
//                    scope.launch { DeviceManager.connectDevice(copy.toDevice(), it.toDevice()) }
                    drawingConnectionSource = null
                } else drawingConnectionSource = it
            }
        )
    }
    AnimatedVisibility(deviceSettingsVisible) {
        selectedDevice?.let {
            AlignedBox(Alignment.BottomStart) {
                Column {
                    DeviceSettings(
                        device = it
                    ) { deviceSettingsVisible = false }
                }
            }
        }

    }
    AlignedBox(Alignment.BottomEnd) {
        AddDeviceButton()
    }
}

fun DrawScope.drawConnection(offset1: Offset, offset2: Offset) {
    drawLine(
        start = offset1,
        end = offset2,
        color = Color(DEVICE_CONNECTION_COLOR),
        strokeWidth = CONNECTION_STROKE_WIDTH
    )
}

fun Offset.isOn(device: Device, image: ImageBitmap): Boolean =
    x in device.x.toFloat()..(device.x.toFloat() + image.width) &&
            y in device.y.toFloat()..(device.y.toFloat() + image.height)

fun ViewDevice.fittingOffset(): Offset {
    val width = (getVector().defaultWidth * devicesViewZoom).toPx()
    val height = (getVector().defaultHeight * devicesViewZoom).toPx()
    return offset + Offset(width, height)
}
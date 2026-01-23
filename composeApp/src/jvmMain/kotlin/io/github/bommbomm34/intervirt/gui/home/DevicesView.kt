package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.onClick
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.are_you_sure_to_remove_connection
import intervirt.composeapp.generated.resources.too_many_devices_connected
import io.github.bommbomm34.intervirt.*
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AcceptDialog
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.AddDeviceButton
import io.github.bommbomm34.intervirt.gui.components.device.settings.DeviceSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.math.sqrt

var drawingConnectionSource: ViewDevice? by mutableStateOf(null)
var deviceSettingsVisible by mutableStateOf(false)

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DevicesView() {
    var selectedDevice: ViewDevice? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val deviceManager = koinInject<DeviceManager>()
    val preferences = koinInject<Preferences>()
    AlignedBox(Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .onPointerEvent(PointerEventType.Scroll) {
                    val delta = it.changes.first().scrollDelta.y * -preferences.ZOOM_SPEED
                    if (isCtrlPressed && devicesViewZoom + delta > 0.1f) devicesViewZoom += delta
                }
                .onClick(matcher = PointerMatcher.Primary) { drawingConnectionSource = null }
                .onClick(matcher = PointerMatcher.Secondary) { drawingConnectionSource = null }
                .onPointerEvent(PointerEventType.Press) { event ->
                    if (event.button?.equals(PointerButton.Secondary) ?: false && drawingConnectionSource == null) {
                        val position = event.changes.first().position
                        statefulConf.connections.firstOrNull { (device1, device2) ->
                            isPointOnLine(
                                point = position,
                                start = device1.fittingOffset(),
                                end = device2.fittingOffset(),
                                strokeWidth = preferences.CONNECTION_STROKE_WIDTH
                            )
                        }?.let {
                            openDialog {
                                AcceptDialog(
                                    message = stringResource(
                                        Res.string.are_you_sure_to_remove_connection,
                                        it.device1.name,
                                        it.device2.name
                                    )
                                ) {
                                    statefulConf.connections.remove(it)
                                    scope.launch {
                                        deviceManager.disconnectDevice(
                                            it.device1.device,
                                            it.device2.device
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                .onKeyEvent {
                    if (it.key == Key.Escape) {
                        drawingConnectionSource = null
                        true
                    } else false
                }
        ) {
            scale(devicesViewZoom) {
                drawingConnectionSource?.let {
                    drawConnection(
                        offset1 = it.fittingOffset(),
                        offset2 = mousePosition,
                        deviceConnectionColor = preferences.DEVICE_CONNECTION_COLOR,
                        connectionStrokeWidth = preferences.CONNECTION_STROKE_WIDTH
                    )
                }
                statefulConf.connections.forEach {
                    drawConnection(
                        offset1 = it.device1.fittingOffset(),
                        offset2 = it.device2.fittingOffset(),
                        deviceConnectionColor = preferences.DEVICE_CONNECTION_COLOR,
                        connectionStrokeWidth = preferences.CONNECTION_STROKE_WIDTH
                    )
                }
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
                    if (copy.id != it.id) {
                        scope.launch {
                            if (copy.canConnect() && it.canConnect()) {
                                statefulConf.connections.add(copy connect it)
                                deviceManager.connectDevice(copy.device, it.device)
                            } else openDialog(
                                importance = Importance.WARNING,
                                message = getString(Res.string.too_many_devices_connected)
                            )
                        }
                    }
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

fun DrawScope.drawConnection(
    offset1: Offset,
    offset2: Offset,
    deviceConnectionColor: Long,
    connectionStrokeWidth: Float
) {
    drawLine(
        start = offset1,
        end = offset2,
        color = Color(deviceConnectionColor),
        strokeWidth = connectionStrokeWidth
    )
}

fun isPointOnLine(
    point: Offset,
    start: Offset,
    end: Offset,
    strokeWidth: Float
): Boolean {
    val dx = end.x - start.x
    val dy = end.y - start.y

    val lengthSquared = dx * dx + dy * dy
    if (lengthSquared == 0f) return false

    val t = ((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSquared

    if (t !in 0f..1f) return false

    val px = start.x + t * dx
    val py = start.y + t * dy

    val distX = point.x - px
    val distY = point.y - py

    val distance = sqrt(distX * distX + distY * distY)

    return distance <= strokeWidth / 2f
}

fun Offset.isOn(device: Device, image: ImageBitmap): Boolean =
    x in device.x.toFloat()..(device.x.toFloat() + image.width) &&
            y in device.y.toFloat()..(device.y.toFloat() + image.height)

fun ViewDevice.fittingOffset(): Offset {
    val width = (getVector().defaultWidth * devicesViewZoom).toPx()
    val height = (getVector().defaultHeight * devicesViewZoom).toPx()
    return offset + Offset(width * 2f, height * 2f)
}
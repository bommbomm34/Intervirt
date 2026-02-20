package io.github.bommbomm34.intervirt.home

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
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.are_you_sure_to_remove_connection
import intervirt.ui.generated.resources.too_many_devices_connected
import io.github.bommbomm34.intervirt.Secondary
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.buttons.AddDeviceButton
import io.github.bommbomm34.intervirt.components.device.settings.DeviceSettings
import io.github.bommbomm34.intervirt.toPx
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DevicesView() {
    var selectedDevice: ViewDevice? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    val appEnv = koinInject<AppEnv>()
    val appState = koinInject<AppState>()
    val configuration = koinInject<IntervirtConfiguration>()
    val statefulConf = appState.statefulConf
    AlignedBox(Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .onPointerEvent(PointerEventType.Scroll) {
                    val delta = it.changes.first().scrollDelta.y * -appEnv.ZOOM_SPEED
                    if (appState.isCtrlPressed && appState.devicesViewZoom + delta > 0.1f) appState.devicesViewZoom += delta
                }
                .onClick(matcher = PointerMatcher.Primary) { appState.drawingConnectionSource = null }
                .onClick(matcher = PointerMatcher.Secondary) { appState.drawingConnectionSource = null }
                .onPointerEvent(PointerEventType.Press) { event ->
                    if (event.button?.equals(PointerButton.Secondary) ?: false && appState.drawingConnectionSource == null) {
                        val position = event.changes.first().position
                        statefulConf.connections.firstOrNull { (device1, device2) ->
                            isPointOnLine(
                                point = position,
                                start = device1.fittingOffset(appState.devicesViewZoom),
                                end = device2.fittingOffset(appState.devicesViewZoom),
                                strokeWidth = appEnv.CONNECTION_STROKE_WIDTH,
                            )
                        }?.let {
                            appState.openDialog {
                                AcceptDialog(
                                    message = stringResource(
                                        Res.string.are_you_sure_to_remove_connection,
                                        it.device1.name,
                                        it.device2.name,
                                    ),
                                ) {
                                    statefulConf.connections.remove(it)
                                    scope.launch {
                                        deviceManager.disconnectDevice(
                                            it.device1.device,
                                            it.device2.device,
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
        ) {
            scale(appState.devicesViewZoom) {
                appState.drawingConnectionSource?.let {
                    drawConnection(
                        offset1 = it.fittingOffset(appState.devicesViewZoom),
                        offset2 = appState.mousePosition,
                        color = appEnv.DEVICE_CONNECTION_COLOR,
                        strokeWidth = appEnv.CONNECTION_STROKE_WIDTH,
                    )
                }
                statefulConf.connections.forEach {
                    drawConnection(
                        offset1 = it.device1.fittingOffset(appState.devicesViewZoom),
                        offset2 = it.device2.fittingOffset(appState.devicesViewZoom),
                        color = appEnv.DEVICE_CONNECTION_COLOR,
                        strokeWidth = appEnv.CONNECTION_STROKE_WIDTH,
                    )
                }
            }
        }
    }

    // This block will most likely be triggered if a file is opened
    selectedDevice?.let { if (!statefulConf.exists(it)) selectedDevice = null }
    appState.drawingConnectionSource?.let { if (!statefulConf.exists(it)) appState.drawingConnectionSource = null }
    if (selectedDevice == null) appState.deviceSettingsVisible = false

    statefulConf.devices.forEach { device ->
        DeviceView(
            device = device,
            onClickDevice = {
                if (selectedDevice != it || !appState.deviceSettingsVisible) {
                    selectedDevice = it
                    appState.deviceSettingsVisible = true
                } else appState.deviceSettingsVisible = false
            },
            onSecondaryClick = {
                val copy = appState.drawingConnectionSource
                if (copy != null) {
                    if (copy.id != it.id) {
                        scope.launch {
                            if (copy.canConnect(configuration) && it.canConnect(configuration)) {
                                statefulConf.connections.add(copy connect it)
                                deviceManager.connectDevice(copy.device, it.device)
                            } else appState.openDialog(
                                severity = Severity.WARNING,
                                message = getString(Res.string.too_many_devices_connected),
                            )
                        }
                    }
                    appState.drawingConnectionSource = null
                } else appState.drawingConnectionSource = it
            },
        )
    }
    AnimatedVisibility(appState.deviceSettingsVisible) {
        selectedDevice?.let {
            AlignedBox(Alignment.BottomStart) {
                Column {
                    DeviceSettings(
                        device = it,
                    ) { appState.deviceSettingsVisible = false }
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
    color: Long,
    strokeWidth: Float,
) {
    drawLine(
        start = offset1,
        end = offset2,
        color = Color(color),
        strokeWidth = strokeWidth,
    )
}

fun isPointOnLine(
    point: Offset,
    start: Offset,
    end: Offset,
    strokeWidth: Float,
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

fun ViewDevice.fittingOffset(devicesViewZoom: Float): Offset {
    val width = (getVector().defaultWidth * devicesViewZoom).toPx()
    val height = (getVector().defaultHeight * devicesViewZoom).toPx()
    return offset + Offset(width * 2f, height * 2f)
}
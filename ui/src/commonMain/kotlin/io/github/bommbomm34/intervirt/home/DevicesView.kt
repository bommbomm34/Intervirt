/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.onClick
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Hub
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.are_you_sure_to_remove_connection
import intervirt.ui.generated.resources.too_many_devices_connected
import io.github.bommbomm34.intervirt.Secondary
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.buttons.AddDeviceButton
import io.github.bommbomm34.intervirt.components.device.settings.DeviceSettings
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.components.dialogs.openAcceptDialog
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.DeviceId
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.connect
import io.github.bommbomm34.intervirt.core.data.getDevice
import io.github.bommbomm34.intervirt.core.data.getDeviceById
import io.github.bommbomm34.intervirt.core.data.getDeviceOrNull
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.currentProject
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.util.ext.toPx
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DevicesView() {
    var selectedDeviceId: DeviceId? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    val appEnv = currentAppEnv
    val appState = koinInject<AppState>()
    val project by currentProject
    var zoom by appState::devicesViewZoom
    val connectionStrokeWidth = appEnv.connectionStrokeWidth
    val density = LocalDensity.current
    Box {
        Canvas(
            Modifier
                .fillMaxSize()
                .onPointerEvent(PointerEventType.Scroll) {
                    val delta = it.changes.first().scrollDelta.y * -appEnv.zoomSpeed
                    if (appState.isCtrlPressed && zoom + delta > 0.1f) zoom += delta
                }
                .onClick(matcher = PointerMatcher.Primary) { appState.drawingConnectionSource = null }
                .onClick(matcher = PointerMatcher.Secondary) { appState.drawingConnectionSource = null }
                .onPointerEvent(PointerEventType.Press) { event ->
                    if (event.button == PointerButton.Secondary && appState.drawingConnectionSource == null) {
                        val position = event.changes.first().position
                        project.connections.firstOrNull {
                            val (device1, device2) = it.getDevices(project.devices)

                            isPointOnLine(
                                point = position,
                                start = device1.fittingOffset(appEnv.deviceScale, density),
                                end = device2.fittingOffset(appEnv.deviceScale, density),
                                strokeWidth = connectionStrokeWidth,
                            )
                        }?.let {
                            val (device1, device2) = it.getDevices(project.devices)
                            appState.openAcceptDialog(
                                Res.string.are_you_sure_to_remove_connection,
                                device1.name,
                                device2.name,
                            ) {
                                close()
                                scope.launchDialogCatching(appState) {
                                    deviceManager.disconnectDevice(device1, device2)
                                }
                            }
                        }
                    }
                },
        ) {
            appState.drawingConnectionSource?.let {
                val device = project.getDeviceById(it) ?: return@let

                drawConnection(
                    offset1 = device.fittingOffset(appEnv.deviceScale, density),
                    offset2 = appState.mousePosition,
                    color = appEnv.deviceConnectionColor,
                    strokeWidth = connectionStrokeWidth,
                )
            }
            project.connections.forEach {
                val (device1, device2) = it.getDevices(project.devices)

                drawConnection(
                    offset1 = device1.fittingOffset(appEnv.deviceScale, density),
                    offset2 = device2.fittingOffset(appEnv.deviceScale, density),
                    color = appEnv.deviceConnectionColor,
                    strokeWidth = connectionStrokeWidth,
                )
            }
        }

        project.devices.forEach { device ->
            DeviceView(
                device = device,
                onClickDevice = {
                    if (selectedDeviceId != it.id || !appState.deviceSettingsVisible) {
                        selectedDeviceId = it.id
                        appState.deviceSettingsVisible = true
                    } else appState.deviceSettingsVisible = false
                },
                onSecondaryClick = {
                    val copy = appState.drawingConnectionSource
                    if (copy != null) {
                        val device = project.getDeviceById(copy) ?: return@DeviceView

                        if (device.id != it.id) {
                            scope.launchDialogCatching(appState) {
                                if (device.canConnect(project) && it.canConnect(project)) {
                                    deviceManager.connectDevice(device, it)
                                } else appState.openDialog(
                                    severity = Severity.WARNING,
                                    message = getString(Res.string.too_many_devices_connected),
                                )
                            }
                        }
                        appState.drawingConnectionSource = null
                    } else appState.drawingConnectionSource = it.id
                },
            )
        }
    }
    LaunchedEffect(project) {
        selectedDeviceId?.let { if (!project.exists(it)) selectedDeviceId = null }
        appState.drawingConnectionSource?.let {
            if (!project.exists(it)) appState.drawingConnectionSource = null
        }
        if (selectedDeviceId == null) appState.deviceSettingsVisible = false
    }
    AnimatedVisibility(appState.deviceSettingsVisible) {
        selectedDeviceId?.let {
            AlignedBox(Alignment.BottomStart) {
                Column {
                    project.getDeviceById(it)?.let { device ->
                        DeviceSettings(
                            device = device,
                        ) { appState.deviceSettingsVisible = false }
                    }
                }
            }
        }

    }
    AlignedBox(Alignment.BottomEnd) {
        AddDeviceButton()
    }
}

private fun DrawScope.drawConnection(
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

private fun isPointOnLine(
    point: Offset,
    start: Offset,
    end: Offset,
    strokeWidth: Float,
): Boolean {
    val dx = end.x - start.x
    val dy = end.y - start.y

    val lengthSquared = dx * dx + dy * dy

    val t = ((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSquared

    if (t !in 0f..1f) return false

    val px = start.x + t * dx
    val py = start.y + t * dy

    val distX = point.x - px
    val distY = point.y - py

    val distance = sqrt(distX * distX + distY * distY)

    return distance <= strokeWidth / 2f
}

private fun Device.fittingOffset(scale: Float, density: Density): Offset {
    // TODO: Design more reliable algorithm
    val padding = DEVICE_PADDING.toPx(density)
    val deviceWidth = vector.defaultWidth.toPx(density) * scale
    val deviceHeight = vector.defaultHeight.toPx(density) * scale
    val width = deviceWidth + padding * 2f
    val height = deviceHeight + padding * 2f + 4.dp.toPx(density)

    return offset + Offset(width / 2f, height / 2f)
}

val Device.vector: ImageVector
    get() = when (this) {
        is Device.Computer -> Icons.Default.Computer
        is Device.Switch -> Icons.Default.Hub
    }

val Device.offset: Offset
    get() = Offset(x.toFloat(), y.toFloat())

private fun Device.canConnect(project: Project): Boolean = when (this) {
    is Device.Computer -> project.connections.count { it.containsDevice(this) } == 0
    is Device.Switch -> true
}

private fun Project.exists(deviceId: DeviceId): Boolean {
    return devices.any { it.id == deviceId }
}

private fun Project.exists(device: Device): Boolean = exists(device.id)

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
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
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
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.util.ext.toPx
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DevicesView() {
    var selectedDevice: ViewDevice? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    val appEnv = currentAppEnv
    val appState = koinInject<AppState>()
    val project by koinInject<Atomic<Project>>()
    val statefulProject = appState.statefulProject
    var zoom by appState::devicesViewZoom
    val connectionStrokeWidth = appEnv.connectionStrokeWidth
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
                    if (event.button?.equals(PointerButton.Secondary) ?: false && appState.drawingConnectionSource == null) {
                        val position = event.changes.first().position
                        statefulProject.connections.firstOrNull { (device1, device2) ->
                            isPointOnLine(
                                point = position,
                                start = device1.fittingOffset(appEnv.deviceScale),
                                end = device2.fittingOffset(appEnv.deviceScale),
                                strokeWidth = connectionStrokeWidth,
                            )
                        }?.let {
                            appState.openAcceptDialog(
                                Res.string.are_you_sure_to_remove_connection,
                                it.device1.name,
                                it.device2.name,
                            ) {
                                close()
                                statefulProject.connections.remove(it)
                                scope.launchDialogCatching(appState) {
                                    deviceManager.disconnectDevice(
                                        it.device1.device,
                                        it.device2.device,
                                    )
                                }
                            }
                        }
                    }
                },
        ) {
            appState.drawingConnectionSource?.let {
                drawConnection(
                    offset1 = it.fittingOffset(appEnv.deviceScale),
                    offset2 = appState.mousePosition,
                    color = appEnv.deviceConnectionColor,
                    strokeWidth = connectionStrokeWidth,
                )
            }
            statefulProject.connections.forEach {
                drawConnection(
                    offset1 = it.device1.fittingOffset(appEnv.deviceScale),
                    offset2 = it.device2.fittingOffset(appEnv.deviceScale),
                    color = appEnv.deviceConnectionColor,
                    strokeWidth = connectionStrokeWidth,
                )
            }
        }

        statefulProject.devices.forEach { device ->
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
                            scope.launchDialogCatching(appState) {
                                if (copy.canConnect(project) && it.canConnect(project)) {
                                    statefulProject.connections.add(copy connect it)
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
    }
    LaunchedEffect(statefulProject) {
        selectedDevice?.let { if (!statefulProject.exists(it)) selectedDevice = null }
        appState.drawingConnectionSource?.let {
            if (!statefulProject.exists(it)) appState.drawingConnectionSource = null
        }
        if (selectedDevice == null) appState.deviceSettingsVisible = false
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

private fun ViewDevice.fittingOffset(scale: Float): Offset {
    // TODO: Design more reliable algorithm
    val padding = DEVICE_PADDING.toPx()
    val deviceWidth = vector.defaultWidth.toPx() * scale
    val deviceHeight = vector.defaultHeight.toPx() * scale
    val width = deviceWidth + padding * 2f
    val height = deviceHeight + padding * 2f + 4.dp.toPx()

    return offset + Offset(width / 2f, height / 2f)
}

// Input: Get IPv6 addresses
// Expected output: ULA addresses
// Actual output: Link-local addresses
// Solution: Only output the IPv6 address which is a ULA (beginning with fdXX) //*

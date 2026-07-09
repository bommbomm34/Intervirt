/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.Secondary
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.util.ext.dpToPx
import io.github.bommbomm34.intervirt.util.ext.toPx
import org.koin.compose.koinInject

val DEVICE_PADDING = 16.dp
private const val MINIMUM_PADDING = 125f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceView(
    device: Device,
    onClickDevice: (Device) -> Unit,
    onSecondaryClick: (Device) -> Unit,
) {
    val appState = koinInject<AppState>()
    val deviceManager = koinInject<DeviceManager>()
    val appEnv = currentAppEnv
    var offset by remember { mutableStateOf(Offset(device.x.toFloat(), device.y.toFloat())) }
    var overlay by remember { mutableStateOf(false) }
    val deviceWidth = dpToPx(device.vector.defaultWidth) * appEnv.deviceScale
    val deviceHeight = dpToPx(device.vector.defaultHeight) * appEnv.deviceScale
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .onClick(
                matcher = PointerMatcher.Primary,
            ) { onClickDevice(device) }
            .onDrag(
                matcher = PointerMatcher.Primary,
                onDragStart = { overlay = true },
                onDragEnd = { overlay = false },
            ) {
                val newOffset = offset + it
                if (newOffset.isOn(
                        dpSize = appState.windowState.size,
                        imageSize = Offset(deviceWidth, deviceHeight),
                        minimumPadding = MINIMUM_PADDING,
                    )
                ) {
                    offset = newOffset

                    deviceManager.move(
                        device = device,
                        x = newOffset.x.toInt(),
                        y = newOffset.y.toInt(),
                    )
                }
            }
            .onClick(
                matcher = PointerMatcher.Secondary,
                onClick = { onSecondaryClick(device) },
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
    ) {
        Column(
            modifier = Modifier.padding(DEVICE_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val deviceWidthDp = remember(device) { device.vector.defaultWidth * appEnv.deviceScale }
            val deviceHeightDp = remember(device) { device.vector.defaultHeight * appEnv.deviceScale }

            Icon(
                imageVector = device.vector,
                contentDescription = device.name,
                modifier = Modifier.size(deviceWidthDp, deviceHeightDp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = if (overlay) 0.5f else 1f),
            )
            GeneralSpacer(2.dp)
            Text(device.name)
        }
    }
}

private fun Offset.isOn(dpSize: DpSize, imageSize: Offset, minimumPadding: Float): Boolean {
    val offsetSize = Offset(dpSize.width.toPx(), dpSize.height.toPx())
    return x <= offsetSize.x - imageSize.x - minimumPadding && y < offsetSize.y - imageSize.y * 2f - minimumPadding &&
            x >= minimumPadding && y >= minimumPadding
}

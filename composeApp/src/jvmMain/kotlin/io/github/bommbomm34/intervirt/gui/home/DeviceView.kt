package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import compose.icons.TablerIcons
import compose.icons.tablericons.DevicesPc
import compose.icons.tablericons.Switch
import io.github.bommbomm34.intervirt.DEVICE_SIZE
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.dpToPx
import io.github.bommbomm34.intervirt.windowState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceView(
    device: ViewDevice,
    onSelectDevice: (ViewDevice) -> Unit
) {
    val getVector: (ViewDevice) -> ImageVector = {
        when (it) {
            is ViewDevice.Switch -> TablerIcons.Switch
            is ViewDevice.Computer -> TablerIcons.DevicesPc
        }
    }
    var offset by remember { mutableStateOf(Offset(device.x.toFloat(), device.y.toFloat())) }
    var overlay by remember { mutableStateOf(false) }
    val deviceSizePx = dpToPx(DEVICE_SIZE)
    Icon(
        imageVector = getVector(device),
        contentDescription = device.name,
        tint = MaterialTheme.colors.onBackground.copy(alpha = if (overlay) 0.5f else 1f),
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .onClick { onSelectDevice(device) }
            .size(DEVICE_SIZE, DEVICE_SIZE)
            .onDrag(
                onDragStart = { overlay = true },
                onDragEnd = { overlay = false }
            ) {
                val newOffset = offset + it
                if (newOffset.isOn(
                        dpSize = windowState.size,
                        imageSize = Offset(deviceSizePx, deviceSizePx),
                        minimumPadding = 140f
                )) {
                    offset = newOffset
                    device.x += it.x.toInt()
                    device.y += it.y.toInt()
                }
            }
    )
}

fun Offset.isOn(dpSize: DpSize, imageSize: Offset, minimumPadding: Float): Boolean {
    val offsetSize = Offset(dpSize.width.value, dpSize.height.value)
    return x <= offsetSize.x - imageSize.x - minimumPadding && y < offsetSize.y - imageSize.y - minimumPadding &&
            x >= minimumPadding && y >= minimumPadding
}
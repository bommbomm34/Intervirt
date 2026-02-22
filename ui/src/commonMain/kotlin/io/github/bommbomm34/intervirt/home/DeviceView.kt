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
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.dpToPx
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceView(
    device: ViewDevice,
    onClickDevice: (ViewDevice) -> Unit,
    onSecondaryClick: (ViewDevice) -> Unit,
) {
    val appState = koinInject<AppState>()
    val appEnv = koinInject<AppEnv>()
    var offset by remember { mutableStateOf(Offset(device.x.toFloat(), device.y.toFloat())) }
    var overlay by remember { mutableStateOf(false) }
    val deviceSizePx = dpToPx(appEnv.DEVICE_SIZE.dp)
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
                        imageSize = Offset(deviceSizePx, deviceSizePx),
                        minimumPadding = 140f,
                    )
                ) {
                    offset = newOffset
                    device.x += it.x.toInt()
                    device.y += it.y.toInt()
                    device.device.x += it.x.toInt()
                    device.device.y += it.y.toInt()
                }
            }
            .onClick(
                matcher = PointerMatcher.Secondary,
                onClick = { onSecondaryClick(device) },
            )
            .clip(RoundedCornerShape(16f))
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background),
    ) {
        Icon(
            imageVector = device.getVector(),
            contentDescription = device.name,
            modifier = Modifier.size(appEnv.DEVICE_SIZE.dp, appEnv.DEVICE_SIZE.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = if (overlay) 0.5f else 1f),
        )
        GeneralSpacer(2.dp)
        Text(device.name)
    }

}

fun Offset.isOn(dpSize: DpSize, imageSize: Offset, minimumPadding: Float): Boolean {
    val offsetSize = Offset(dpSize.width.value, dpSize.height.value)
    return x <= offsetSize.x - imageSize.x - minimumPadding && y < offsetSize.y - imageSize.y - minimumPadding &&
            x >= minimumPadding && y >= minimumPadding
}
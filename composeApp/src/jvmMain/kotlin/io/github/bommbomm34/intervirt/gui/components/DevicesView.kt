package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.computer
import io.github.bommbomm34.intervirt.CONNECTION_STROKE_WIDTH
import io.github.bommbomm34.intervirt.DEVICE_CONNECTION_COLOR
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.DeviceConnection
import io.github.bommbomm34.intervirt.devicesViewZoom
import org.jetbrains.compose.resources.imageResource

@Composable
fun DevicesView() {
    val computerImage = imageResource(Res.drawable.computer)
    AlignedBox(Alignment.Center) {
        Canvas(Modifier.fillMaxSize(0.5f)) {
            scale(devicesViewZoom){
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

fun DrawScope.drawDevice(device: Device, image: ImageBitmap){
    drawImage(
        image = image,
        topLeft = Offset(device.x, device.y)
    )
}

fun DrawScope.drawConnection(connection: DeviceConnection){
    drawLine(
        start = Offset(connection.device1.x, connection.device1.y),
        end = Offset(connection.device2.x, connection.device2.y),
        color = Color(DEVICE_CONNECTION_COLOR),
        strokeWidth = CONNECTION_STROKE_WIDTH
    )
}
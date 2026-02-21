package io.github.bommbomm34.intervirt.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.pick_color
import io.github.bommbomm34.intervirt.data.AppState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ColorPicker(
    color: Color,
    onColorSelect: (Color) -> Unit,
) {
    val appState = koinInject<AppState>()
    CenterRow {
        ColorCircle(color, 32.dp)
        GeneralSpacer()
        Button(
            onClick = {
                appState.openDialog {
                    ColorPickerDialog(color, onColorSelect, ::close)
                }
            },
        ) {
            Text(stringResource(Res.string.pick_color))
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    radius: Dp,
) {
    Box(
        modifier = Modifier
            .size(radius * 0.9f)
            .background(color = color, shape = CircleShape)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = CircleShape,
            ),
    )
}

@Composable
private fun ColorPickerDialog(
    color: Color,
    onColorSelect: (Color) -> Unit,
    onClose: () -> Unit,
) {
    CenterColumn {
        var color by remember { mutableStateOf(color) }
        val controller = rememberColorPickerController()
        HsvColorPicker(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .padding(16.dp),
            controller = controller,
            onColorChanged = { color = it.color },
            initialColor = remember { color },
        )
        GeneralSpacer()
        Button(
            onClick = {
                onColorSelect(color)
                onClose()
            },
        ) {
            Text(stringResource(Res.string.pick_color))
        }
    }
}
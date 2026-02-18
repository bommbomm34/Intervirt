package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.core.readablePercentage

@Composable
fun ProgressView(
    progress: Float,
    message: String = "",
    messageColor: Color = MaterialTheme.colors.onBackground,
) {
    CenterColumn {
        SelectionContainer {
            Text(
                text = message,
                color = messageColor,
            )
        }
        GeneralSpacer(4.dp)
        LinearProgressIndicator(progress = progress)
        GeneralSpacer(4.dp)
        Text(progress.readablePercentage())
    }
}
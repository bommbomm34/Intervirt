package io.github.bommbomm34.intervirt.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.core.readablePercentage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressView(
    progress: Float,
    message: String = "",
    messageColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    CenterColumn {
        SelectionContainer {
            Text(
                text = message,
                color = messageColor,
            )
        }
        GeneralSpacer(4.dp)
        LinearWavyProgressIndicator(progress = { animatedProgress })
        GeneralSpacer(4.dp)
        Text(progress.readablePercentage())
    }
}
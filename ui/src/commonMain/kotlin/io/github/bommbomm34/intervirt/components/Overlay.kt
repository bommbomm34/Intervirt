package io.github.bommbomm34.intervirt.components

import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Overlay(
    alpha: Float = 0.5f,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.safeContentPadding(),
        color = Color.Black.copy(alpha = alpha),
        content = content,
    )
}
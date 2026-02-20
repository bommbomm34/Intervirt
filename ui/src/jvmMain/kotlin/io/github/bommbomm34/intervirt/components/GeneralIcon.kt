package io.github.bommbomm34.intervirt.components

import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun GeneralIcon(
    imageVector: ImageVector,
    contentDescription: String,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = MaterialTheme.colors.onBackground,
    )
}
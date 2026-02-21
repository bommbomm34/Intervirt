package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun NextButton(
    visible: Boolean,
    onClick: () -> Unit,
) {
    if (visible) {
        IconButton(
            onClick = onClick,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
            )
        }
    }
}
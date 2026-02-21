package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft

@Composable
fun BackButton(
    visible: Boolean = true,
    onClick: () -> Unit,
) {
    if (visible) {
        IconButton(
            onClick = onClick,
        ) {
            Icon(
                imageVector = TablerIcons.ArrowLeft,
                contentDescription = "Back",
            )
        }
    }
}
package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft

@Composable
fun BackButton(
    visible: Boolean,
    onClick: () -> Unit
) {
    if (visible){
        IconButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = TablerIcons.ArrowLeft,
                contentDescription = "Back",
                tint = MaterialTheme.colors.onBackground
            )
        }
    }
}
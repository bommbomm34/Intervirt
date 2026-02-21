package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight

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
                imageVector = TablerIcons.ArrowRight,
                contentDescription = "Next",
            )
        }
    }
}
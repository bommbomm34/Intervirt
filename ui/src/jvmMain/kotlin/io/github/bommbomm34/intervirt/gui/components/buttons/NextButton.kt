package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight

@Composable
fun NextButton(
    visible: Boolean,
    onClick: () -> Unit
){
    if (visible){
        IconButton(
            onClick = onClick
        ){
            Icon(
                imageVector = TablerIcons.ArrowRight,
                contentDescription = "Next",
                tint = MaterialTheme.colors.onBackground
            )
        }
    }
}
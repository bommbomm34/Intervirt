package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import compose.icons.TablerIcons
import compose.icons.tablericons.Trash
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.delete
import org.jetbrains.compose.resources.stringResource

@Composable
fun RemoveButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = TablerIcons.Trash,
            contentDescription = stringResource(Res.string.delete),
            tint = Color.Red
        )
    }
}
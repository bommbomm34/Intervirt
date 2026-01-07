package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.add
import intervirt.composeapp.generated.resources.delete
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = TablerIcons.Plus,
            contentDescription = stringResource(Res.string.add)
        )
    }
}
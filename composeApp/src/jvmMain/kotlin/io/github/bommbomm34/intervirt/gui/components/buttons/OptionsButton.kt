package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.DotsVertical
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.options
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import org.jetbrains.compose.resources.stringResource

@Composable
fun OptionsButton(onClick: () -> Unit) {
    IconButton(onClick){
        GeneralIcon(
            imageVector = TablerIcons.DotsVertical,
            contentDescription = stringResource(Res.string.options)
        )
    }
}
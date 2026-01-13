package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.DotsVertical
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.options
import org.jetbrains.compose.resources.stringResource

@Composable
fun OptionsButton(onClick: () -> Unit){
    IconButton(
        onClick = onClick
    ){
        Icon(
            imageVector = TablerIcons.DotsVertical,
            contentDescription = stringResource(Res.string.options)
        )
    }
}
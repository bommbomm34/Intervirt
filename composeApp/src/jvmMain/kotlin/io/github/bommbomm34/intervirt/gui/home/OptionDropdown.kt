package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Settings
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.about
import intervirt.composeapp.generated.resources.settings
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.IconText
import io.github.bommbomm34.intervirt.showSettings
import org.jetbrains.compose.resources.stringResource

@Composable
fun OptionDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit
){
    Column {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ){
            DropdownMenuItem(
                onClick = { showSettings = true }
            ){
                IconText(
                    imageVector = TablerIcons.Settings,
                    text = stringResource(Res.string.settings)
                )
            }
            DropdownMenuItem(
                onClick = {
                    // TODO: Show About window
                }
            ){
                IconText(
                    imageVector = TablerIcons.InfoCircle,
                    text = stringResource(Res.string.about)
                )
            }
        }
    }
}
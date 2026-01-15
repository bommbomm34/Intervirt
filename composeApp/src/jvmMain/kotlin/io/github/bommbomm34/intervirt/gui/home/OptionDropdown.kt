package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Settings
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.about
import intervirt.composeapp.generated.resources.save
import intervirt.composeapp.generated.resources.save_as
import intervirt.composeapp.generated.resources.settings
import io.github.bommbomm34.intervirt.currentScreenIndex
import io.github.bommbomm34.intervirt.gui.components.buttons.IconText
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
            // Save
            DropdownMenuItem(
                onClick = {
                    // TODO: Save file
                }
            ){
                IconText(
                    imageVector = TablerIcons.DeviceFloppy,
                    text = stringResource(Res.string.save)
                )
            }
            // Save As
            DropdownMenuItem(
                onClick = {
                    // TODO: Save file as
                }
            ){
                IconText(
                    imageVector = TablerIcons.DeviceFloppy,
                    text = stringResource(Res.string.save_as)
                )
            }
            // Settings
            DropdownMenuItem(
                onClick = { currentScreenIndex = 2 }
            ){
                IconText(
                    imageVector = TablerIcons.Settings,
                    text = stringResource(Res.string.settings)
                )
            }
            // About
            DropdownMenuItem(
                onClick = { currentScreenIndex = 3 }
            ){
                IconText(
                    imageVector = TablerIcons.InfoCircle,
                    text = stringResource(Res.string.about)
                )
            }
        }
    }
}
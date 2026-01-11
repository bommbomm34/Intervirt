package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.DevicesPc
import compose.icons.tablericons.Switch
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.computer
import intervirt.composeapp.generated.resources.switch
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddDeviceButton() {
    var dropdownExpanded by remember { mutableStateOf(false) }
    Column {
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    // TODO: Add computer
                }
            ) {
                Icon(
                    imageVector = TablerIcons.DevicesPc,
                    contentDescription = stringResource(Res.string.computer)
                )
                GeneralSpacer(5.dp)
                Text(stringResource(Res.string.computer))
            }
            DropdownMenuItem(
                onClick = {
                    // TODO: Add switch
                }
            ) {
                Icon(
                    imageVector = TablerIcons.Switch,
                    contentDescription = stringResource(Res.string.computer)
                )
                GeneralSpacer(5.dp)
                Text(stringResource(Res.string.switch))
            }
        }
        AddButton { dropdownExpanded = true }
    }
}
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
import intervirt.composeapp.generated.resources.default_computer_name
import intervirt.composeapp.generated.resources.default_switch_name
import intervirt.composeapp.generated.resources.os_is_needed
import intervirt.composeapp.generated.resources.switch
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.closeDialog
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.stateful.toViewDevice
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.imagepicker.ImageIcon
import io.github.bommbomm34.intervirt.gui.imagepicker.ImagePicker
import io.github.bommbomm34.intervirt.openDialog
import io.github.bommbomm34.intervirt.statefulConf
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddDeviceButton() {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val osIsNeededText = stringResource(Res.string.os_is_needed)
    Column {
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    dropdownExpanded = false
                    // Add computer
                    openDialog {
                        ImagePicker(
                            onDismiss = {
                                openDialog(
                                    message = osIsNeededText,
                                    importance = Importance.ERROR
                                )
                            },
                            onInstall = {
                                scope.launch {
                                    val device = DeviceManager.addComputer(
                                        name = getString(Res.string.default_computer_name),
                                        x = 300,
                                        y = 300,
                                        image = it.fullName()
                                    )
                                    statefulConf.devices.add(device.toViewDevice())
                                }
                            }
                        )
                    }
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
                    dropdownExpanded = false
                    // Add switch
                    scope.launch {
                        val device = DeviceManager.addSwitch(
                            name = getString(Res.string.default_switch_name),
                            x = 300,
                            y = 300
                        )
                        statefulConf.devices.add(device.toViewDevice())
                    }
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
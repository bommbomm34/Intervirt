package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.DevicesPc
import compose.icons.tablericons.Switch
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.data.stateful.toViewDevice
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.imagepicker.ImagePicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.random.Random

@Composable
fun AddDeviceButton() {
    val appState = koinInject<AppState>()
    var dropdownExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val osIsNeededText = stringResource(Res.string.os_is_needed)
    val deviceManager = koinInject<DeviceManager>()
    Column {
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    dropdownExpanded = false
                    // Add computer
                    appState.openDialog {
                        ImagePicker(
                            onDismiss = {
                                appState.openDialog(
                                    message = osIsNeededText,
                                    importance = Importance.ERROR
                                )
                            },
                            onInstall = { image ->
                                scope.launch {
                                    deviceManager.addComputer(
                                        x = Random.nextInt(300, 600),
                                        y = Random.nextInt(300, 600),
                                        image = image.fullName
                                    )
                                        .onSuccess {
                                            appState.statefulConf.devices.add(it.toViewDevice())
                                            appState.closeDialog()
                                        }
                                        .onFailure {
                                            appState.openDialog(
                                                importance = Importance.ERROR,
                                                message = getString(
                                                    Res.string.error_during_device_creation,
                                                    it.localizedMessage
                                                )
                                            )
                                        }
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
                    val device = deviceManager.addSwitch(
                        x = Random.nextInt(300, 600),
                        y = Random.nextInt(300, 600)
                    )
                    appState.statefulConf.devices.add(device.toViewDevice())
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
package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.CenterRow
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import io.github.bommbomm34.intervirt.data.toViewDevice
import io.github.bommbomm34.intervirt.imagepicker.ImagePicker
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
            onDismissRequest = { dropdownExpanded = false },
        ) {
            DropdownMenuItem(
                onClick = {
                    dropdownExpanded = false
                    // Add computer
                    appState.openDialog {
                        ImagePicker(
                            onDismiss = {
                                close()
                                appState.openDialog(
                                    message = osIsNeededText,
                                    severity = Severity.ERROR,
                                )
                            },
                            onInstall = { image ->
                                close()
                                scope.launchDialogCatching(appState) {
                                    val viewDevice = deviceManager.addComputer(
                                        x = Random.nextInt(300, 600),
                                        y = Random.nextInt(300, 600),
                                        image = image.fullName,
                                    ).getOrThrow().toViewDevice()
                                    appState.statefulConf.devices.add(viewDevice)
                                }
                            },
                        )
                    }
                },
                text = {
                    CenterRow {
                        IconText(
                            imageVector = Icons.Default.Computer,
                            text = stringResource(Res.string.computer),
                        )
                    }
                },
            )
            DropdownMenuItem(
                onClick = {
                    dropdownExpanded = false
                    // Add switch
                    val device = deviceManager.addSwitch(
                        x = Random.nextInt(300, 600),
                        y = Random.nextInt(300, 600),
                    )
                    appState.statefulConf.devices.add(device.toViewDevice())
                },
                text = {
                    IconText(
                        imageVector = Icons.Default.Hub,
                        text = stringResource(Res.string.switch),
                    )
                },
            )
        }
        AddButton { dropdownExpanded = true }
    }
}
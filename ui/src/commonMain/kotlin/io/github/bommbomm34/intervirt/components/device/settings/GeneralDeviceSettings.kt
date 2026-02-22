package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.are_you_sure_to_remove_device
import intervirt.ui.generated.resources.delete
import intervirt.ui.generated.resources.name
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun GeneralDeviceSettings(
    device: ViewDevice,
    onClose: () -> Unit,
) {
    val deviceManager = koinInject<DeviceManager>()
    val appState = koinInject<AppState>()
    val scope = rememberCoroutineScope()
    OutlinedTextField(
        value = device.id,
        onValueChange = {}, // ID can't be changed once set
        enabled = false,
        label = { Text("ID") },
    )
    GeneralSpacer()
    OutlinedTextField(
        value = device.name,
        onValueChange = { newName ->
            device.name = newName
            deviceManager.setName(device.device, newName)
        },
        label = { Text(stringResource(Res.string.name)) },
    )
    GeneralSpacer()
    Button(
        onClick = {
            appState.openDialog {
                AcceptDialog(
                    message = stringResource(Res.string.are_you_sure_to_remove_device, device.name),
                    onCancel = ::close,
                ) {
                    scope.launchDialogCatching(appState) {
                        close()
                        onClose()
                        appState.statefulConf.devices.remove(device)
                        appState.statefulConf.connections.removeIf { it.containsDevice(device) }
                        deviceManager.removeDevice(device.device).getOrThrow()
                    }
                }
            }

        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
    ) {
        Text(
            text = stringResource(Res.string.delete),
            color = Color.White,
        )
    }
}
package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.are_you_sure_to_remove_device
import intervirt.ui.generated.resources.delete
import intervirt.ui.generated.resources.name
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AcceptDialog
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun GeneralDeviceSettings(
    device: ViewDevice,
    onClose: () -> Unit
) {
    val deviceManager = koinInject <DeviceManager>()
    val appState = koinInject<AppState>()
    val scope = rememberCoroutineScope()
    OutlinedTextField(
        value = device.id,
        onValueChange = {}, // ID can't be changed once set
        enabled = false,
        label = { Text("ID") }
    )
    GeneralSpacer()
    OutlinedTextField(
        value = device.name,
        onValueChange = { newName ->
            device.name = newName
            deviceManager.setName(device.device, newName)
        },
        label = { Text(stringResource(Res.string.name)) }
    )
    GeneralSpacer()
    Button(
        onClick = {
            appState.openDialog {
                AcceptDialog(
                    message = stringResource(Res.string.are_you_sure_to_remove_device, device.name),
                ) {
                    scope.launch {
                        onClose()
                        appState.statefulConf.devices.remove(device)
                        appState.statefulConf.connections.removeIf { it.containsDevice(device) }
                        deviceManager.removeDevice(device.device)
                    }
                }
            }

        },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
    ){
        Text(
            text = stringResource(Res.string.delete),
            color = Color.White
        )
    }
}
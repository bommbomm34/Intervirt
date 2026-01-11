package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.are_you_sure_to_remove_device
import intervirt.composeapp.generated.resources.delete
import intervirt.composeapp.generated.resources.name
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AcceptDialog
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.openDialog
import io.github.bommbomm34.intervirt.statefulConf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun GeneralDeviceSettings(
    device: ViewDevice,
    onClose: () -> Unit
) {
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
            DeviceManager.setName(device.toDevice(), newName)
        },
        label = { Text(stringResource(Res.string.name)) }
    )
    GeneralSpacer()
    Button(
        onClick = {
            openDialog {
                AcceptDialog(
                    message = stringResource(Res.string.are_you_sure_to_remove_device, device.name),
                ) {
                    scope.launch {
                        onClose()
                        statefulConf.devices.remove(device)
                        DeviceManager.removeDevice(device.toDevice())
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
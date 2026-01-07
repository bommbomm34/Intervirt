package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.name
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.logger
import org.jetbrains.compose.resources.stringResource

@Composable
fun GeneralDeviceSettings(device: Device) {
    OutlinedTextField(
        value = device.id,
        onValueChange = {}, // ID can't be changed once set
        enabled = false,
        label = { Text("ID") }
    )
    GeneralSpacer()
    OutlinedTextField(
        value = configuration.devices.first { it.id == device.id }.name,
        onValueChange = { newName ->
            logger.debug { "Setting name of ${device.id} to $newName" }
            DeviceManager.setName(device, newName)
        },
        label = { Text(stringResource(Res.string.name)) }
    )
}
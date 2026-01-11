package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.install_new_os
import intervirt.composeapp.generated.resources.os
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.toReadableImage
import org.jetbrains.compose.resources.stringResource

@Composable
fun OSField(device: ViewDevice.Computer){
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = device.image.toReadableImage(),
            onValueChange = {},
            label = { Text(stringResource(Res.string.os)) },
            enabled = false
        )
        GeneralSpacer()
        Button(
            onClick = {
                // TODO: Install new OS
            },
        ) {
            Text(stringResource(Res.string.install_new_os))
        }
    }
}
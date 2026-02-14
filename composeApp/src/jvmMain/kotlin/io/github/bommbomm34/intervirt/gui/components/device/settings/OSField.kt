package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.os
import io.github.bommbomm34.intervirt.data.ViewDevice
import org.jetbrains.compose.resources.stringResource

@Composable
fun OSField(device: ViewDevice.Computer){
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = device.image,
            onValueChange = {},
            label = { Text(stringResource(Res.string.os)) },
            enabled = false
        )
    }
}
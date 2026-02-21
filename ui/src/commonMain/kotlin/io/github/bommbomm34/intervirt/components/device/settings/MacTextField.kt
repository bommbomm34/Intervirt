package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.mac_address
import io.github.bommbomm34.intervirt.data.ViewDevice
import org.jetbrains.compose.resources.stringResource

@Composable
fun MACTextField(device: ViewDevice.Computer) {
    OutlinedTextField(
        value = device.mac,
        onValueChange = {}, // MAC address can't be changed once set
        enabled = false,
        label = { Text(stringResource(Res.string.mac_address)) },
    )
}
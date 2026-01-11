package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.invalid_ipv4_address
import intervirt.composeapp.generated.resources.ipv4_address
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import kotlinx.coroutines.launch
import org.apache.commons.validator.routines.InetAddressValidator
import org.jetbrains.compose.resources.stringResource

@Composable
fun IPv4TextField(device: ViewDevice.Computer){
    var validIPv4 by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    OutlinedTextField(
        value = device.ipv4,
        onValueChange = {
            scope.launch {
                validIPv4 = InetAddressValidator.getInstance().isValidInet4Address(it)
                if (validIPv4) {
                    device.ipv4 = it
                    DeviceManager.setIPv4(device.toDevice(), it)
                }
            }
        },
        label = {
            if (validIPv4){
                Text(stringResource(Res.string.ipv4_address))
            } else {
                Text(
                    text = stringResource(Res.string.invalid_ipv4_address),
                    color = Color.Red
                )
            }
        }
    )
}
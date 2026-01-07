package io.github.bommbomm34.intervirt.gui.components

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
import intervirt.composeapp.generated.resources.invalid_ipv6_address
import intervirt.composeapp.generated.resources.ipv4_address
import intervirt.composeapp.generated.resources.ipv6_address
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Device
import kotlinx.coroutines.launch
import org.apache.commons.validator.routines.InetAddressValidator
import org.jetbrains.compose.resources.stringResource

@Composable
fun IPv6TextField(device: Device.Computer){
    var validIPv6 by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    OutlinedTextField(
        value = device.ipv4,
        onValueChange = {
            scope.launch {
                validIPv6 = InetAddressValidator.getInstance().isValidInet6Address(it)
                if (validIPv6) DeviceManager.setIPv6(device, it)
            }
        },
        label = {
            if (validIPv6){
                Text(stringResource(Res.string.ipv6_address))
            } else {
                Text(
                    text = stringResource(Res.string.invalid_ipv6_address),
                    color = Color.Red
                )
            }
        }
    )
}
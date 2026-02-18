package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.invalid_ipv4_address
import intervirt.ui.generated.resources.ipv4_address
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.ViewDevice
import kotlinx.coroutines.launch
import org.apache.commons.validator.routines.InetAddressValidator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Ipv4TextField(device: ViewDevice.Computer) {
    var validIpv4 by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    OutlinedTextField(
        value = device.ipv4,
        onValueChange = {
            scope.launch {
                validIpv4 = InetAddressValidator.getInstance().isValidInet4Address(it)
                if (validIpv4) {
                    device.ipv4 = it
                    deviceManager.setIpv4(device.device, it)
                }
            }
        },
        label = {
            if (validIpv4) {
                Text(stringResource(Res.string.ipv4_address))
            } else {
                Text(
                    text = stringResource(Res.string.invalid_ipv4_address),
                    color = Color.Red,
                )
            }
        },
    )
}
package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.invalid_ipv4_address
import intervirt.ui.generated.resources.ipv4_address
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import org.apache.commons.validator.routines.InetAddressValidator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Ipv4TextField(
    ipv4: String,
    onIpv4Change: (String) -> Unit
) {
    var validIpv4 by remember { mutableStateOf(true) }
    OutlinedTextField(
        value = ipv4,
        onValueChange = {
            validIpv4 = InetAddressValidator.getInstance().isValidInet4Address(it)
            if (validIpv4) onIpv4Change(it)
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
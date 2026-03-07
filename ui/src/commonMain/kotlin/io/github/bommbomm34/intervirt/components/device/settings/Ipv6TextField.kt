/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.invalid_ipv6_address
import intervirt.ui.generated.resources.ipv6_address
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import org.apache.commons.validator.routines.InetAddressValidator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Ipv6TextField(
    ipv6: String,
    onIpv6Change: (String) -> Unit,
) {
    var validIpv6 by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    val appState = koinInject<AppState>()
    OutlinedTextField(
        value = ipv6,
        onValueChange = {
            validIpv6 = InetAddressValidator.getInstance().isValidInet4Address(it)
            if (validIpv6) onIpv6Change(it)
        },
        label = {
            if (validIpv6) {
                Text(stringResource(Res.string.ipv6_address))
            } else {
                Text(
                    text = stringResource(Res.string.invalid_ipv6_address),
                    color = Color.Red,
                )
            }
        },
    )
}
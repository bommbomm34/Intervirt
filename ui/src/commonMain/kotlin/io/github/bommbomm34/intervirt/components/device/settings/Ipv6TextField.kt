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
import io.github.bommbomm34.intervirt.core.util.validateIpv6
import org.jetbrains.compose.resources.stringResource

@Composable
fun Ipv6TextField(
    ipv6: String,
    onIpv6Change: (String) -> Unit,
) {
    var validIpv6 by remember { mutableStateOf(true) }
    OutlinedTextField(
        value = ipv6,
        onValueChange = {
            validIpv6 = it.validateIpv6()
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
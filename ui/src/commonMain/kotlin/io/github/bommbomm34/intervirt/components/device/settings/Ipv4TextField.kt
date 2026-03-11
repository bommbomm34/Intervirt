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
import intervirt.ui.generated.resources.invalid_ipv4_address
import intervirt.ui.generated.resources.ipv4_address
import io.github.bommbomm34.intervirt.core.util.validateIpv4
import org.jetbrains.compose.resources.stringResource

@Composable
fun Ipv4TextField(
    ipv4: String,
    onIpv4Change: (String) -> Unit,
) {
    var validIpv4 by remember { mutableStateOf(true) }
    OutlinedTextField(
        value = ipv4,
        onValueChange = {
            validIpv4 = it.validateIpv4()
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
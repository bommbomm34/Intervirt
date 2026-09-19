/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.invalid_ipv6_address
import intervirt.ui.generated.resources.ipv4_address
import intervirt.ui.generated.resources.ipv6_address
import io.github.bommbomm34.intervirt.core.data.AgentInfo
import io.github.bommbomm34.intervirt.core.util.isIPWithinSubnet
import io.github.bommbomm34.intervirt.core.util.validateIpv4
import io.github.bommbomm34.intervirt.core.util.validateIpv6
import io.github.bommbomm34.intervirt.data.IPErrorReason
import org.jetbrains.compose.resources.stringResource

@Composable
fun Ipv6TextField(
    info: AgentInfo,
    ipv6: String,
    onIpv6Change: (String) -> Unit,
) {
    var errorReason: IPErrorReason? by remember { mutableStateOf(null) }

    OutlinedTextField(
        value = ipv6,
        onValueChange = {
            errorReason = when {
                !validateIpv6(it) -> IPErrorReason.Invalid
                !it.isIPWithinSubnet(info.ipv6Subnet) -> IPErrorReason.NotWithinSubnet
                else -> {
                    onIpv6Change(it)
                    null
                }
            }
        },
        label = {
            val errorReason = errorReason

            if (errorReason == null) {
                Text(stringResource(Res.string.ipv6_address))
            } else {
                Text(
                    text = errorReason.getErrorMessage(info.ipv6Subnet, isIpv6 = true),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

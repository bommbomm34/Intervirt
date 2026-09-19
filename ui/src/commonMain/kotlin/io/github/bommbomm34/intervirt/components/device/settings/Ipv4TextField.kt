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
import intervirt.ui.generated.resources.invalid_ipv4_address
import intervirt.ui.generated.resources.ipv4_address
import io.github.bommbomm34.intervirt.core.data.AgentInfo
import io.github.bommbomm34.intervirt.core.util.isIPWithinSubnet
import io.github.bommbomm34.intervirt.core.util.validateIpv4
import io.github.bommbomm34.intervirt.data.IPErrorReason
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import sun.security.krb5.KrbException.errorMessage

@Composable
fun Ipv4TextField(
    info: AgentInfo,
    ipv4: String,
    onIpv4Change: (String) -> Unit,
) {
    var errorReason: IPErrorReason? by remember { mutableStateOf(null) }

    OutlinedTextField(
        value = ipv4,
        onValueChange = {
            errorReason = when {
                !validateIpv4(it) -> IPErrorReason.Invalid
                !it.isIPWithinSubnet(info.ipv4Subnet) -> IPErrorReason.NotWithinSubnet
                else -> {
                    onIpv4Change(it)
                    null
                }
            }
        },
        label = {
            val errorReason = errorReason

            if (errorReason == null) {
                Text(stringResource(Res.string.ipv4_address))
            } else {
                Text(
                    text = errorReason.getErrorMessage(info.ipv4Subnet, isIpv6 = false),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

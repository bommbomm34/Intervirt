/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.Composable
import inet.ipaddr.IPAddress
import intervirt.ui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

sealed class IPErrorReason {
    @Composable
    abstract fun getErrorMessage(subnet: IPAddress, isIpv6: Boolean): String

    object Invalid : IPErrorReason() {
        @Composable
        override fun getErrorMessage(subnet: IPAddress, isIpv6: Boolean): String {
            return stringResource(if (isIpv6) Res.string.invalid_ipv6_address else Res.string.invalid_ipv4_address)
        }
    }

    object NotWithinSubnet : IPErrorReason() {
        @Composable
        override fun getErrorMessage(subnet: IPAddress, isIpv6: Boolean): String {
            val res =
                if (isIpv6) Res.string.ipv6_address_not_within_subnet else Res.string.ipv4_address_not_within_subnet

            return stringResource(res, subnet)
        }
    }
}

package io.github.bommbomm34.intervirt.components

import androidx.compose.runtime.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.external_port
import intervirt.ui.generated.resources.internal_port
import io.github.bommbomm34.intervirt.components.textfields.IntegerTextField
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import org.jetbrains.compose.resources.stringResource

private val PROTOCOLS = listOf("TCP", "UDP")

@Composable
fun PortForwardingChooser(
    portForwarding: PortForwarding,
    onChangePortForwarding: (PortForwarding) -> Unit,
) {
    var portForwarding by remember { mutableStateOf(PortForwarding("tcp", 0, 0)) }
    CenterRow {
        IntegerTextField(
            value = portForwarding.hostPort,
            onValueChange = { portForwarding = portForwarding.copy(hostPort = it) },
            label = stringResource(Res.string.external_port),
        )
        GeneralSpacer()
        IntegerTextField(
            value = portForwarding.guestPort,
            onValueChange = { portForwarding = portForwarding.copy(guestPort = it) },
            label = stringResource(Res.string.internal_port),
        )
        GeneralSpacer()
        SelectionDropdown(
            options = PROTOCOLS,
            selected = portForwarding.protocol.uppercase(),
            onSelect = { portForwarding = portForwarding.copy(protocol = it.lowercase()) },
        )
    }
}
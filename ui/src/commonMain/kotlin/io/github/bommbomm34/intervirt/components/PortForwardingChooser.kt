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
    var portForwarding by remember { mutableStateOf(portForwarding) }
    CenterRow {
        IntegerTextField(
            value = portForwarding.externalPort,
            onValueChange = {
                portForwarding = portForwarding.copy(externalPort = it)
                onChangePortForwarding(portForwarding)
            },
            label = stringResource(Res.string.external_port),
        )
        GeneralSpacer()
        IntegerTextField(
            value = portForwarding.internalPort,
            onValueChange = {
                portForwarding = portForwarding.copy(internalPort = it)
                onChangePortForwarding(portForwarding)
            },
            label = stringResource(Res.string.internal_port),
        )
        GeneralSpacer()
        SelectionDropdown(
            options = PROTOCOLS,
            selected = portForwarding.protocol.uppercase(),
            onSelect = {
                portForwarding = portForwarding.copy(protocol = it.lowercase())
                onChangePortForwarding(portForwarding)
            },
        )
    }
}
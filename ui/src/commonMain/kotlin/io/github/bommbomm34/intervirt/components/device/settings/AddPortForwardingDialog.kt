/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.add_port_forwarding
import intervirt.ui.generated.resources.external_port_already_bound
import intervirt.ui.generated.resources.internal_port_already_exposed
import io.github.bommbomm34.intervirt.canPortBind
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.PortForwardingChooser
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AddPortForwardingDialog(
    onAdd: (PortForwarding) -> Unit,
    onLint: suspend (PortForwarding) -> Result<Unit>,
    onCancel: () -> Unit,
) {
    CenterColumn {
        var portForwarding by remember { mutableStateOf(PortForwarding.DEFAULT) }
        var result by remember { mutableStateOf(Result.success(Unit)) }
        PortForwardingChooser(
            portForwarding = portForwarding,
            onChangePortForwarding = { portForwarding = it },
        )
        LaunchedEffect(portForwarding) {
            result = onLint(portForwarding)
        }
        if (result.isFailure) {
            result.exceptionOrNull()?.let { exp ->
                GeneralSpacer()
                Text(
                    text = exp.localizedMessage,
                    color = Color.Red,
                )
            }
        }
        GeneralSpacer()
        Row {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                )
            }
            GeneralSpacer()
            Button(
                onClick = {
                    onAdd(portForwarding)
                    onCancel()
                },
                enabled = result.isSuccess,
            ) {
                Text(stringResource(Res.string.add_port_forwarding))
            }
        }
    }
}
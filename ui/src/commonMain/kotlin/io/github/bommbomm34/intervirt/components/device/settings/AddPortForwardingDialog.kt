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
import arrow.core.Either
import arrow.core.right
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.add_port_forwarding
import intervirt.ui.generated.resources.cancel
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.PortForwardingChooser
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPortForwardingDialog(
    onAdd: (PortForwarding) -> Unit,
    onLint: suspend (PortForwarding) -> Either<Failure.PortForwardingValidationFailure, Unit>,
    onCancel: () -> Unit,
) {
    CenterColumn {
        var portForwarding by remember { mutableStateOf(PortForwarding.DEFAULT) }
        var result: Either<Failure.PortForwardingValidationFailure, Unit> by remember { mutableStateOf(Unit.right()) }
        PortForwardingChooser(
            portForwarding = portForwarding,
            onChangePortForwarding = { portForwarding = it },
        )
        LaunchedEffect(portForwarding) {
            result = onLint(portForwarding)
        }
        result.onLeft {
            GeneralSpacer()
            Text(
                text = it.message,
                color = Color.Red,
            )
        }
        GeneralSpacer()
        Row {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            ) {
                Text(
                    text = stringResource(Res.string.cancel),
                    color = Color.White,
                )
            }
            GeneralSpacer()
            Button(
                onClick = {
                    onAdd(portForwarding)
                    onCancel()
                },
                enabled = result.isRight(),
            ) {
                Text(stringResource(Res.string.add_port_forwarding))
            }
        }
    }
}

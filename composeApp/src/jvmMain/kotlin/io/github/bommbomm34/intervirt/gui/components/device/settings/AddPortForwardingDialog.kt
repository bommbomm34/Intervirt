package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.canPortBind
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.IntegerTextField
import io.github.bommbomm34.intervirt.isValidPort
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AddPortForwardingDialog(
    device: ViewDevice.Computer,
    onCancel: () -> Unit
) {
    CenterColumn {
        var internalPort by remember { mutableStateOf(1) }
        var externalPort by remember { mutableStateOf(1) }
        var result by remember { mutableStateOf(Result.success(Unit)) }
        val scope = rememberCoroutineScope()
        val deviceManager = koinInject<DeviceManager>()
        Row(verticalAlignment = Alignment.CenterVertically) {
            IntegerTextField(
                value = internalPort,
                onValueChange = { if (it.isValidPort()) internalPort = it },
                label = stringResource(Res.string.internal_port)
            )
            Text(
                text = ":",
                fontWeight = FontWeight.ExtraBold
            )
            IntegerTextField(
                value = externalPort,
                onValueChange = { if (it.isValidPort()) externalPort = it },
                label = stringResource(Res.string.external_port)
            )
        }
        LaunchedEffect(internalPort, externalPort) {
            result = lint(device, internalPort, externalPort)
        }
        if (result.isFailure) {
            result.exceptionOrNull()?.let { exp ->
                GeneralSpacer()
                Text(
                    text = exp.localizedMessage,
                    color = Color.Red
                )
            }
        }
        GeneralSpacer()
        Row {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ){
                Text(
                    text = "Cancel",
                    color = Color.White
                )
            }
            GeneralSpacer()
            Button(
                onClick = {
                    scope.launch {
                        device.portForwardings[internalPort] = externalPort
                        deviceManager.addPortForwarding(device.device, internalPort, externalPort)
                        onCancel()
                    }
                },
                enabled = result.isSuccess
            ) {
                Text(stringResource(Res.string.add_port_forwarding))
            }
        }
    }
}

suspend fun lint(
    device: ViewDevice.Computer,
    internalPort: Int,
    externalPort: Int
): Result<Unit> {
    val bindResult = externalPort.canPortBind()
    return when {
        device.portForwardings.contains(internalPort) -> Result.failure(IllegalArgumentException(getString(Res.string.internal_port_already_exposed)))
        configuration.devices.any { if (it is Device.Computer) it.portForwardings.containsValue(externalPort) else false } -> Result.failure(
            IllegalArgumentException(getString(Res.string.external_port_already_bound))
        )

        bindResult.isFailure -> Result.failure(bindResult.exceptionOrNull()!!)
        else -> Result.success(Unit)
    }
}
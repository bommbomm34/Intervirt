package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.*
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val protocols = listOf("TCP", "UDP")

@Composable
fun AddPortForwardingDialog(
    device: ViewDevice.Computer,
    onCancel: () -> Unit,
) {
    CenterColumn {
        var portForwarding by remember { mutableStateOf(PortForwarding.DEFAULT) }
        var result by remember { mutableStateOf(Result.success(Unit)) }
        val scope = rememberCoroutineScope()
        val deviceManager = koinInject<DeviceManager>()
        val configuration = koinInject<IntervirtConfiguration>()
        val appState = koinInject<AppState>()
        PortForwardingChooser(
            portForwarding = portForwarding,
            onChangePortForwarding = { portForwarding = it },
        )
        LaunchedEffect(portForwarding.externalPort, portForwarding.internalPort) {
            result = configuration.lint(device, portForwarding)
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
                    scope.launchDialogCatching(appState) {
                        device.portForwardings.add(portForwarding)
                        deviceManager.addPortForwarding(device.device, portForwarding).getOrThrow()
                        onCancel()
                    }
                },
                enabled = result.isSuccess,
            ) {
                Text(stringResource(Res.string.add_port_forwarding))
            }
        }
    }
}


private suspend fun IntervirtConfiguration.lint(
    device: ViewDevice.Computer,
    portForwarding: PortForwarding,
): Result<Unit> {
    val bindResult = portForwarding.externalPort.canPortBind()
    return when {
        device.portForwardings.any { it.internalPort == portForwarding.internalPort } -> Result.failure(
            IllegalArgumentException(
                getString(
                    Res.string.internal_port_already_exposed,
                ),
            ),
        )

        devices.any { device ->
            if (device is Device.Computer) device.portForwardings.any {
                it.externalPort == portForwarding.externalPort && it.protocol == portForwarding.protocol
            } else false
        } -> Result.failure(
            IllegalArgumentException(getString(Res.string.external_port_already_bound)),
        )

        bindResult.isFailure -> Result.failure(bindResult.exceptionOrNull()!!)
        else -> Result.success(Unit)
    }
}
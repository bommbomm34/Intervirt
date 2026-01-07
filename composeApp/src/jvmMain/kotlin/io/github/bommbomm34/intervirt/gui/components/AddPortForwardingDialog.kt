package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.add_port_forwarding
import intervirt.composeapp.generated.resources.external_port
import intervirt.composeapp.generated.resources.external_port_already_bound
import intervirt.composeapp.generated.resources.internal_port
import intervirt.composeapp.generated.resources.internal_port_already_exposed
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.canPortBind
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.isValidPort
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.w3c.dom.Text
import java.net.ServerSocket

@Composable
fun AddPortForwardingDialog(
    device: Device.Computer,
    onCancel: () -> Unit
) {
    var internalPort by remember { mutableStateOf(1) }
    var externalPort by remember { mutableStateOf(1) }
    var result by remember { mutableStateOf(Result.success(Unit)) }
    val scope = rememberCoroutineScope()
    Dialog {
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
        LaunchedEffect(internalPort, externalPort){
            result = lint(device, internalPort, externalPort)
        }
        if (result.isFailure){
            result.exceptionOrNull()?.let { exp ->
                Text(
                    text = exp.localizedMessage,
                    color = Color.Red
                )
            }
        }
        GeneralSpacer()
        Button(
            onClick = {
                scope.launch {
                    DeviceManager.addPortForwarding(device, internalPort, externalPort)
                }
            },
            enabled = result.isSuccess
        ){
            Text(stringResource(Res.string.add_port_forwarding))
        }
    }
}

suspend fun lint(
    device: Device.Computer,
    internalPort: Int,
    externalPort: Int
): Result<Unit> {
    val bindResult = externalPort.canPortBind()
    return when {
        device.portForwardings.contains(internalPort) -> Result.failure(IllegalArgumentException(getString(Res.string.internal_port_already_exposed)))
        configuration.devices.any { if (it is Device.Computer) it.portForwardings.containsValue(externalPort) else false } -> Result.failure(IllegalArgumentException(getString(Res.string.external_port_already_bound)))
        bindResult.isFailure -> Result.failure(bindResult.exceptionOrNull()!!)
        else -> Result.success(Unit)
    }
}
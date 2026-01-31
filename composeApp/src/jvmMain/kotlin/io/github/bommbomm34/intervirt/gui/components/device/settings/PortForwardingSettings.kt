package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.DialogState
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.buttons.AddButton
import io.github.bommbomm34.intervirt.gui.components.buttons.RemoveButton
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PortForwardingSettings(device: ViewDevice.Computer) {
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    val appState = koinInject<AppState>()
    Column {
        AddButton {
            appState.openDialog {
                AddPortForwardingDialog(
                    device
                ) { appState.dialogState = (appState.dialogState as DialogState.Custom).copy(visible = false) }
            }
        }
        LazyColumn {
            items(device.portForwardings.toList()) { portForwarding ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${portForwarding.first}:${portForwarding.second}")
                    RemoveButton {
                        scope.launch {
                            device.portForwardings.remove(portForwarding.first)
                            // TODO: Support UDP protocol
                            deviceManager.removePortForwarding(portForwarding.second, "tcp")
                        }
                    }
                }
            }
        }
    }
}
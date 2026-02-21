package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.DialogState
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.components.buttons.AddButton
import io.github.bommbomm34.intervirt.components.buttons.RemoveButton
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
                    device = device,
                    onCancel = ::close
                )
            }
        }
        LazyColumn {
            items(device.portForwardings) { portForwarding ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${portForwarding.protocol}:${portForwarding.guestPort}:${portForwarding.hostPort}")
                    RemoveButton {
                        scope.launch {
                            device.portForwardings.remove(portForwarding)
                            deviceManager.removePortForwarding(portForwarding.hostPort, portForwarding.protocol)
                        }
                    }
                }
            }
        }
    }
}
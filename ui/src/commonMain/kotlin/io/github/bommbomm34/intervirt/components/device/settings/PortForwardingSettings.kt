package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import io.github.bommbomm34.intervirt.components.buttons.AddButton
import io.github.bommbomm34.intervirt.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun PortForwardingSettings(device: ViewDevice.Computer) {
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    val appState = koinInject<AppState>()
    Column {
        AddButton {
            appState.openDialog(width = 800.dp) {
                AddPortForwardingDialog(
                    device = device,
                    onCancel = ::close,
                )
            }
        }
        LazyColumn {
            items(device.portForwardings) { portForwarding ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${portForwarding.protocol}:${portForwarding.internalPort}:${portForwarding.externalPort}")
                    RemoveButton {
                        scope.launchDialogCatching(appState) {
                            device.portForwardings.remove(portForwarding)
                            deviceManager.removePortForwarding(portForwarding.externalPort, portForwarding.protocol).getOrThrow()
                        }
                    }
                }
            }
        }
    }
}
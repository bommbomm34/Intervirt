package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.AddButton
import io.github.bommbomm34.intervirt.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import org.koin.compose.koinInject

@Composable
fun PortForwardingSettings(device: ViewDevice.Computer) {
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    val appState = koinInject<AppState>()
    val appEnv = koinInject<AppEnv>()
    val fabMod = remember { Modifier.size(appEnv.SMALL_FAB_SIZE.dp) }
    Column {
        AddButton(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = fabMod,
        ) {
            appState.openDialog(width = 800.dp) {
                AddPortForwardingDialog(
                    device = device,
                    onCancel = ::close,
                )
            }
        }
        GeneralSpacer()
        LazyColumn {
            items(device.portForwardings) { portForwarding ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${portForwarding.protocol}:${portForwarding.internalPort}:${portForwarding.externalPort}")
                    GeneralSpacer(4.dp)
                    RemoveButton(fabMod) {
                        scope.launchDialogCatching(appState) {
                            device.portForwardings.remove(portForwarding)
                            deviceManager.removePortForwarding(portForwarding.externalPort, portForwarding.protocol)
                                .getOrThrow()
                        }
                    }
                }
            }
        }
    }
}
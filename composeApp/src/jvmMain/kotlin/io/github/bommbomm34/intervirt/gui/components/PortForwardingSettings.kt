package io.github.bommbomm34.intervirt.gui.components

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
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.dialogState
import io.github.bommbomm34.intervirt.openDialog
import kotlinx.coroutines.launch

@Composable
fun PortForwardingSettings(device: ViewDevice.Computer) {
    val scope = rememberCoroutineScope()
    Column {
        AddButton {
            openDialog {
                AddPortForwardingDialog(
                    device
                ) { dialogState = (dialogState as DialogState.Custom).copy(visible = false) }
            }
        }
        LazyColumn {
            items(device.portForwardings.toList()) { portForwarding ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${portForwarding.first}:${portForwarding.second}")
                    RemoveButton {
                        scope.launch {
                            device.portForwardings.remove(portForwarding.first)
                            DeviceManager.removePortForwarding(portForwarding.second)
                        }
                    }
                }
            }
        }
    }
}
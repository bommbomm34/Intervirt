package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Device
import kotlinx.coroutines.launch

@Composable
fun PortForwardingSettings(device: Device.Computer) {
    val scope = rememberCoroutineScope()
    var addDialogVisible by remember { mutableStateOf(false) }
    AddButton { addDialogVisible = true }
    LazyColumn {
        items(device.portForwardings.toList()) { portForwarding ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${portForwarding.first}:${portForwarding.second}")
                GeneralSpacer()
                RemoveButton {
                    scope.launch {
                        DeviceManager.removePortForwarding(portForwarding.second)
                    }
                }
            }
        }
    }
    // Add port forwarding dialog
    AnimatedVisibility(addDialogVisible){
        AddPortForwardingDialog(device){ addDialogVisible = false }
    }
}
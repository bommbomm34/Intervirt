package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.hide_port_forwardings
import intervirt.ui.generated.resources.show_port_forwardings
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeviceSettings(
    device: ViewDevice,
    onClose: () -> Unit
) {
    var showPortForwardings by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))) {
        // Device settings
        Column {
            CloseButton(onClose)
            GeneralSpacer()
            GeneralDeviceSettings(device) { onClose() }
            GeneralSpacer()
            if (device is ViewDevice.Computer) {
                // All other device settings except port forwardings
                // Device settings specific for computers
                AnimatedVisibility(!showPortForwardings){
                    Column {
                        OSField(device)
                        GeneralSpacer()
                        IOOptions(device)
                        GeneralSpacer()
                        Ipv4TextField(device)
                        GeneralSpacer()
                        Ipv6TextField(device)
                        GeneralSpacer()
                        MACTextField(device)
                        GeneralSpacer()
                        InternetEnabledOption(device)
                        GeneralSpacer()
                    }
                }
                AnimatedVisibility(showPortForwardings){
                    PortForwardingSettings(device)
                }
                // Show/Hide port forwardings
                Button(
                    onClick = { showPortForwardings = !showPortForwardings }
                ) {
                    Text(stringResource(if (showPortForwardings) Res.string.hide_port_forwardings else Res.string.show_port_forwardings))
                }
            }
        }
    }
}
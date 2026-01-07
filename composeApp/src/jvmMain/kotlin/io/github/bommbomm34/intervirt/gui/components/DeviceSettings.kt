package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import compose.icons.TablerIcons
import compose.icons.tablericons.X
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.close
import intervirt.composeapp.generated.resources.hide_port_forwardings
import intervirt.composeapp.generated.resources.show_port_forwardings
import io.github.bommbomm34.intervirt.data.Device
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeviceSettings(
    device: Device,
    onClose: () -> Unit
) {
    var showPortForwardings by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))) {
        // Device settings
        Column {
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier.background(Color.Red)
            ) {
                Icon(
                    imageVector = TablerIcons.X,
                    contentDescription = stringResource(Res.string.close),
                    tint = Color.White
                )
            }
            GeneralSpacer()
            GeneralDeviceSettings(device)
            GeneralSpacer()
            if (device is Device.Computer) {
                MultipleAnimatedVisibility(
                    visible = if (showPortForwardings) 1 else 0,
                    screens = listOf(
                        {
                            // All other device settings except port forwardings
                            // Device settings specific for computers
                            OSField(device)
                            GeneralSpacer()
                            IPv4TextField(device)
                            GeneralSpacer()
                            IPv6TextField(device)
                            GeneralSpacer()
                            InternetEnabledOption(device)
                            GeneralSpacer()
                            // Show/Hide port forwardings
                            Button(
                                onClick = { showPortForwardings = !showPortForwardings }
                            ) {
                                Text(stringResource(if (showPortForwardings) Res.string.show_port_forwardings else Res.string.hide_port_forwardings))
                            }
                        },
                        { PortForwardingSettings(device) }
                    )
                )
            }
        }
    }
}
package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.hide_port_forwardings
import intervirt.ui.generated.resources.show_port_forwardings
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.model.DeviceSettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeviceSettings(
    device: ViewDevice,
    onClose: () -> Unit,
) {
    val viewModel = koinViewModel<DeviceSettingsViewModel> { parametersOf(device) }
    Surface(modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))) {
        // Device settings
        Column {
            CloseButton(onClose)
            GeneralSpacer()
            GeneralDeviceSettings(device, onClose)
            GeneralSpacer()
            if (device is ViewDevice.Computer) {
                // All other device settings except port forwardings
                // Device settings specific for computers
                AnimatedVisibility(!viewModel.showPortForwardings) {
                    Column {
                        OSField(device)
                        GeneralSpacer()
                        // IOOptions and start/stop button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IOOptions(
                                onDownload = viewModel::download,
                                onUpload = viewModel::upload,
                                onOpenShell = viewModel::openShell,
                            )
                            GeneralSpacer()
                            ComputerStartButton(device, viewModel::start, viewModel::stop)
                        }
                        GeneralSpacer()
                        Ipv4TextField(device)
                        GeneralSpacer()
                        Ipv6TextField(device)
                        GeneralSpacer()
                        MacTextField(device)
                        GeneralSpacer()
                        InternetEnabledOption(device)
                        GeneralSpacer()
                    }
                }
                AnimatedVisibility(viewModel.showPortForwardings) {
                    PortForwardingSettings(device)
                }
                GeneralSpacer()
                // Show/Hide port forwardings
                Button(
                    onClick = viewModel::togglePortForwardings,
                ) {
                    Text(stringResource(if (viewModel.showPortForwardings) Res.string.hide_port_forwardings else Res.string.show_port_forwardings))
                }
            }
        }
    }
}
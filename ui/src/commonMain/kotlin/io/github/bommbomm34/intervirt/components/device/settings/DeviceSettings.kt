/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

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
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.excludeHidden
import io.github.bommbomm34.intervirt.data.hasIntervirtOS
import io.github.bommbomm34.intervirt.model.DeviceSettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeviceSettings(
    device: Device,
    onClose: () -> Unit,
) {
    val viewModel = koinViewModel<DeviceSettingsViewModel>(key = device.id) { parametersOf(device.id) }

    Surface(modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))) {
        // Device settings
        Column {
            CloseButton(onClose)
            GeneralSpacer()
            GeneralDeviceSettings(device, onClose)
            GeneralSpacer()
            if (device is Device.Computer) {
                viewModel.info?.let { info ->
                    // All other device settings except port forwardings
                    // Device settings specific for computers
                    AnimatedVisibility(!viewModel.showPortForwardings) {
                        Column {
                            OSField(device)
                            GeneralSpacer()
                            // IOOptions and start/stop button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IOOptions(
                                    isIntervirtOS = device.hasIntervirtOS(),
                                    onDownload = viewModel::download,
                                    onUpload = viewModel::upload,
                                    onOpenShell = viewModel::openShell,
                                )
                                GeneralSpacer()
                                ComputerStartButton(device, viewModel::start, viewModel::stop)
                            }
                            GeneralSpacer()
                            Ipv4TextField(info, device.ipv4, viewModel::changeIpv4)
                            GeneralSpacer()
                            Ipv6TextField(info, device.ipv6, viewModel::changeIpv6)
                            GeneralSpacer()
                            MacTextField(device)
                            GeneralSpacer()
                            InternetEnabledOption(device.internetEnabled, viewModel::enableInternetAccess)
                            GeneralSpacer()
                        }
                    }
                    AnimatedVisibility(viewModel.showPortForwardings) {
                        val portForwardings = device.portForwardings.excludeHidden()
                        PortForwardingSettings(
                            portForwardings = portForwardings,
                            onAdd = viewModel::openAddPortForwarding,
                            onRemove = viewModel::removePortForwarding,
                        )
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
}

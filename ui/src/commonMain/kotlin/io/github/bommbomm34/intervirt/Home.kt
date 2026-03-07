/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.buttons.OptionsButton
import io.github.bommbomm34.intervirt.home.DevicesView
import io.github.bommbomm34.intervirt.home.OptionDropdown
import io.github.bommbomm34.intervirt.home.VMManagerView
import io.github.bommbomm34.intervirt.model.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Home() {
    val viewModel = koinViewModel<HomeViewModel>()
    key(viewModel.devicesViewRenderKey) { DevicesView() }
    VMManagerView(
        running = viewModel.vmRunning,
        onBoot = viewModel::boot,
        onReboot = viewModel::reboot,
        onSync = viewModel::sync,
    )
    AlignedBox(Alignment.BottomCenter) {
        Text(viewModel.getZoom())
    }
    AlignedBox(Alignment.TopEnd) {
        OptionsButton { viewModel.showOptions = true }
        OptionDropdown(viewModel)
    }
}
package io.github.bommbomm34.intervirt

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.core.roundBy
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.OptionsButton
import io.github.bommbomm34.intervirt.gui.home.DevicesView
import io.github.bommbomm34.intervirt.gui.home.OptionDropdown
import io.github.bommbomm34.intervirt.gui.home.VMManagerView
import org.koin.compose.koinInject

@Composable
fun Home() {
    val appState = koinInject<AppState>()
    var devicesViewRenderKey by remember { mutableStateOf(0) }
    key(devicesViewRenderKey) { _root_ide_package_.io.github.bommbomm34.intervirt.home.DevicesView() }
    _root_ide_package_.io.github.bommbomm34.intervirt.home.VMManagerView()
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.BottomCenter) {
        Text("${appState.devicesViewZoom.roundBy(1)}x")
    }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.TopEnd) {
        var showOptions by remember { mutableStateOf(false) }
        _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.OptionsButton { showOptions = true }
        _root_ide_package_.io.github.bommbomm34.intervirt.home.OptionDropdown(
            expanded = showOptions,
            onConfChange = { devicesViewRenderKey++ },
        ) { showOptions = false }
    }
}
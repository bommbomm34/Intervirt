package io.github.bommbomm34.intervirt.gui

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.OptionsButton
import io.github.bommbomm34.intervirt.gui.home.DevicesView
import io.github.bommbomm34.intervirt.gui.home.OptionDropdown
import io.github.bommbomm34.intervirt.gui.home.VMManagerView
import io.github.bommbomm34.intervirt.roundBy
import org.koin.compose.koinInject

@Composable
fun Home() {
    val appState = koinInject<AppState>()
    var devicesViewRenderKey by remember { mutableStateOf(0) }
    key (devicesViewRenderKey) { DevicesView() }
    VMManagerView()
    AlignedBox(Alignment.BottomCenter) {
        Text("${appState.devicesViewZoom.roundBy(1)}x")
    }
    AlignedBox(Alignment.TopEnd) {
        var showOptions by remember { mutableStateOf(false) }
        OptionsButton { showOptions = true }
        OptionDropdown(
            expanded = showOptions,
            onConfChange = { devicesViewRenderKey++ }
        ) { showOptions = false }
    }
}
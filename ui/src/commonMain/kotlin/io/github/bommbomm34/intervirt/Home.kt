package io.github.bommbomm34.intervirt

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.buttons.OptionsButton
import io.github.bommbomm34.intervirt.core.roundBy
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.home.DevicesView
import io.github.bommbomm34.intervirt.home.OptionDropdown
import io.github.bommbomm34.intervirt.home.VMManagerView
import org.koin.compose.koinInject

@Composable
fun Home() {
    val appState = koinInject<AppState>()
    var devicesViewRenderKey by remember { mutableStateOf(0) }
    key(devicesViewRenderKey) { DevicesView() }
    VMManagerView()
    AlignedBox(Alignment.BottomCenter) {
        Text("${appState.devicesViewZoom.roundBy(1)}x")
    }
    AlignedBox(Alignment.TopEnd) {
        var showOptions by remember { mutableStateOf(false) }
        OptionsButton { showOptions = true }
        OptionDropdown(
            expanded = showOptions,
            onConfChange = { devicesViewRenderKey++ },
        ) { showOptions = false }
    }
}
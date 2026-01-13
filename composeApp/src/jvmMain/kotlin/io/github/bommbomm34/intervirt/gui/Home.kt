package io.github.bommbomm34.intervirt.gui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.devicesViewZoom
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.OptionsButton
import io.github.bommbomm34.intervirt.gui.home.DevicesView
import io.github.bommbomm34.intervirt.gui.home.OptionDropdown
import io.github.bommbomm34.intervirt.gui.home.VMManagerView
import io.github.bommbomm34.intervirt.roundBy
import io.github.bommbomm34.intervirt.showSettings

@Composable
fun Home(){
    VMManagerView()
    DevicesView()
    AlignedBox(Alignment.BottomStart){
        Text("${devicesViewZoom.roundBy(1)}x")
    }
    AlignedBox(Alignment.TopEnd){
        var showOptions by remember { mutableStateOf(false) }
        OptionsButton { showOptions = true }
        OptionDropdown(showOptions){ showOptions = false }
    }
}
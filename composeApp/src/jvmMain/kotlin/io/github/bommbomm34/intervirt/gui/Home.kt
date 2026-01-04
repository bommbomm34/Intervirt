package io.github.bommbomm34.intervirt.gui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.dk.kuiver.model.buildKuiver
import io.github.bommbomm34.intervirt.devicesViewZoom
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.DevicesView
import io.github.bommbomm34.intervirt.gui.components.VMManagerView
import io.github.bommbomm34.intervirt.roundBy

@Composable
fun Home(){
    VMManagerView()
    DevicesView()
    AlignedBox(Alignment.BottomStart){
        Text("${devicesViewZoom.roundBy(1)}x")
    }
}
package io.github.bommbomm34.intervirt.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.dk.kuiver.model.buildKuiver
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.VMManagerView

@Composable
fun Home(){
    val kuiver = remember {
        buildKuiver {  }
    }
    VMManagerView()
    AlignedBox(Alignment.Center){

    }
}
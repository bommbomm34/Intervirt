package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.RebootButton
import io.github.bommbomm34.intervirt.gui.components.buttons.ShutdownButton
import io.github.bommbomm34.intervirt.gui.components.buttons.SyncButton

@Composable
fun VMManagerView() {
    AlignedBox(Alignment.TopStart, padding = 16.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShutdownButton()
            GeneralSpacer(4.dp)
            RebootButton()
        }
    }
    AlignedBox(Alignment.TopEnd, padding = 16.dp) {
        SyncButton()
    }
}
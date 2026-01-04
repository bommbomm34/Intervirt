package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun VMManagerView() {
    AlignedBox(Alignment.TopStart) {
        CenterColumn {
            ShutdownButton()
            GeneralSpacer()
            RebootButton()
        }
    }
    SyncButton()
}
package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterRow
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.RebootButton
import io.github.bommbomm34.intervirt.gui.components.buttons.ShutdownButton
import io.github.bommbomm34.intervirt.gui.components.buttons.SyncButton
import org.koin.compose.koinInject

@Composable
fun VMManagerView() {
    val qemuClient = koinInject <QemuClient>()
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(Unit){
        qemuClient.onRunningChange { running = it }
    }
    AlignedBox(Alignment.TopStart, padding = 16.dp) {
        CenterRow {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ShutdownButton()
                GeneralSpacer(4.dp)
                RebootButton(running)
            }
            GeneralSpacer()
            SyncButton(running)
        }
    }
}
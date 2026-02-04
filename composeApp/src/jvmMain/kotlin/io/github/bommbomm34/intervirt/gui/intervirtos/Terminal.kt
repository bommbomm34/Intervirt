package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.awt.SwingPanel
import io.github.bommbomm34.intervirt.api.IntervirtOSClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Component

@Composable
fun Terminal(
    osClient: IntervirtOSClient
) {
    var terminalFactory: (() -> Component)? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // TODO: Use SSH for terminal
        }
    }
    terminalFactory?.let {
        SwingPanel(
            factory = it
        )
    }
}
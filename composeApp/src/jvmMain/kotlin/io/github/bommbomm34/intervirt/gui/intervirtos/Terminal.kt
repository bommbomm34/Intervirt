package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.awt.SwingPanel
import io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Component

@Composable
fun Terminal(
    bundle: ContainerClientBundle
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
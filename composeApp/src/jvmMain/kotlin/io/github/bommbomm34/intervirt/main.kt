package io.github.bommbomm34.intervirt

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.bommbomm34.intervirt.api.QEMUInterface
import kotlinx.coroutines.launch
import org.slf4j.simple.SimpleLogger

fun main() = application {
    if (DEBUG_ENABLED) System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
    val scope = rememberCoroutineScope()
    Window(
        onCloseRequest = {
            scope.launch {
                qemu.shutdownAlpine()
                exitApplication()
            }
        },
        title = "Intervirt",
    ) {
        App()
    }
}
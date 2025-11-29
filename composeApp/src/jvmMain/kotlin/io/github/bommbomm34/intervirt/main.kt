package io.github.bommbomm34.intervirt

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.slf4j.simple.SimpleLogger

fun main() = application {
    if (DEBUG_ENABLED) System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
    Window(
        onCloseRequest = ::exitApplication,
        title = "Intervirt",
    ) {
        App()
    }
}
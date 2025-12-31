package io.github.bommbomm34.intervirt

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.bommbomm34.intervirt.api.QEMUClient
import io.github.bommbomm34.intervirt.gui.App
import kotlinx.coroutines.launch
import org.slf4j.simple.SimpleLogger

fun main() = application {
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
    val scope = rememberCoroutineScope()
    Window(
        onCloseRequest = {
            scope.launch {
                QEMUClient.shutdownAlpine()
                exitApplication()
            }
        },
        title = "Intervirt",
    ) {
        App()
    }
}
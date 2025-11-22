package io.github.bommbomm34.intervirt

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Intervirt",
    ) {
        App()
    }
}
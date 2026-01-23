package io.github.bommbomm34.intervirt

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.api.QemuClient
import io.github.bommbomm34.intervirt.gui.App
import io.github.bommbomm34.intervirt.gui.LogsView
import io.github.bommbomm34.intervirt.gui.components.DefaultWindowScope
import io.github.bommbomm34.intervirt.gui.components.Dialog
import io.github.bommbomm34.intervirt.gui.home.deviceSettingsVisible
import io.github.bommbomm34.intervirt.gui.home.drawingConnectionSource
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.slf4j.simple.SimpleLogger
import java.util.*

fun main() = application {
    KoinApplication(application = {
        modules(mainModule)
    }) {
        val preferences = koinInject<Preferences>()
        val qemuClient = koinInject<QemuClient>()
        if (preferences.checkSetupStatus()) currentScreenIndex = 1
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
        FileKit.init("intervirt")
        Locale.setDefault(preferences.LANGUAGE)
        density = LocalDensity.current
        val scope = rememberCoroutineScope { Dispatchers.IO }
        // Main Window
        Window(
            onCloseRequest = {
                scope.launch {
                    qemuClient.shutdownAlpine()
                    exitApplication()
                }
            },
            onKeyEvent = {
                isCtrlPressed = it.isCtrlPressed
                if (it.key == Key.Escape) {
                    if (drawingConnectionSource != null) {
                        drawingConnectionSource = null
                        true
                    } else if (deviceSettingsVisible) {
                        deviceSettingsVisible = false
                        true
                    } else false
                } else false
            },
            state = windowState,
            title = "Intervirt",
        ) {
            DefaultWindowScope(onPointerEvent = { mousePosition = it.changes.first().position }) {
                App()
                Dialog()
            }
        }
        // Logs Window
        Window(
            onCloseRequest = { showLogs = false },
            visible = showLogs,
            title = "Intervirt Logs",
            state = rememberWindowState(position = WindowPosition.Aligned(Alignment.CenterEnd))
        ) {
            DefaultWindowScope {
                LogsView(logs)
            }
        }
    }
}
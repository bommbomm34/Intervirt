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
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.terminal_window_title
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.api.QemuClient
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.gui.App
import io.github.bommbomm34.intervirt.gui.LogsView
import io.github.bommbomm34.intervirt.gui.ShellView
import io.github.bommbomm34.intervirt.gui.components.DefaultWindowScope
import io.github.bommbomm34.intervirt.gui.components.Dialog
import io.github.bommbomm34.intervirt.gui.home.deviceSettingsVisible
import io.github.bommbomm34.intervirt.gui.home.drawingConnectionSource
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import java.util.*

fun main() = application {
    KoinApplication(application = {
        modules(mainModule)
    }) {
        val preferences = koinInject<Preferences>()
        val qemuClient = koinInject<QemuClient>()
        val appState = koinInject<AppState>()
        if (preferences.checkSetupStatus()) appState.currentScreenIndex = 1
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
                appState.isCtrlPressed = it.isCtrlPressed
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
            state = appState.windowState,
            title = "Intervirt",
        ) {
            DefaultWindowScope(onPointerEvent = { appState.mousePosition = it.changes.first().position }) {
                App()
                Dialog()
            }
        }
        // Logs Window
        Window(
            onCloseRequest = { appState.showLogs = false },
            visible = appState.showLogs,
            title = "Intervirt Logs",
            state = rememberWindowState(position = WindowPosition.Aligned(Alignment.CenterEnd))
        ) {
            DefaultWindowScope {
                LogsView(appState.logs)
            }
        }
        // Shell Window
        Window(
            onCloseRequest = { appState.openComputerShell = null },
            visible = appState.openComputerShell != null,
            title = stringResource(Res.string.terminal_window_title, appState.openComputerShell?.name ?: "")
        ){
            DefaultWindowScope {
                appState.openComputerShell?.let { ShellView(it) }
            }
        }
    }
}
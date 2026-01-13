package io.github.bommbomm34.intervirt

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.intervirt_settings
import io.github.bommbomm34.intervirt.api.QEMUClient
import io.github.bommbomm34.intervirt.gui.App
import io.github.bommbomm34.intervirt.gui.LogsView
import io.github.bommbomm34.intervirt.gui.Settings
import io.github.bommbomm34.intervirt.gui.components.DefaultWindowScope
import io.github.bommbomm34.intervirt.gui.components.Dialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.slf4j.simple.SimpleLogger
import kotlin.random.Random

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
        onKeyEvent = {
            isCtrlPressed = it.isCtrlPressed
            false
        },
        state = windowState,
        title = "Intervirt",
    ) {
        DefaultWindowScope {
            App()
            Dialog()
        }
    }
    Window(
        onCloseRequest = { showLogs = false },
        visible = showLogs,
        title = "Intervirt Logs",
        state = rememberWindowState(position = WindowPosition.Aligned(Alignment.CenterEnd))
    ){
        DefaultWindowScope {
            LogsView(logs)
        }
    }
    Window(
        onCloseRequest = { showSettings = false },
        visible = showSettings,
        title = stringResource(Res.string.intervirt_settings),
        state = rememberWindowState(position = WindowPosition.Aligned(Alignment.CenterEnd))
    ){
        DefaultWindowScope {
            Settings()
        }
    }
}
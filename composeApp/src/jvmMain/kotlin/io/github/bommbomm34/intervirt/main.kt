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
import io.github.bommbomm34.intervirt.api.QEMUClient
import io.github.bommbomm34.intervirt.gui.App
import io.github.bommbomm34.intervirt.gui.LogsView
import io.github.bommbomm34.intervirt.gui.components.Dialog
import kotlinx.coroutines.launch
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
            Dialog()
            App()
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
}

@Composable
fun DefaultWindowScope(content: @Composable BoxScope.() -> Unit){
    val colors = if (env("DARK_THEME")?.toBoolean() ?: isSystemInDarkTheme()) darkColors() else lightColors()
    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography.copy(
            h1 = MaterialTheme.typography.h1.copy(colors.onBackground),
            h2 = MaterialTheme.typography.h2.copy(colors.onBackground),
            h3 = MaterialTheme.typography.h3.copy(colors.onBackground),
            h4 = MaterialTheme.typography.h4.copy(colors.onBackground),
            h5 = MaterialTheme.typography.h5.copy(colors.onBackground),
            h6 = MaterialTheme.typography.h6.copy(colors.onBackground),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(colors.onBackground),
            subtitle2 = MaterialTheme.typography.subtitle2.copy(colors.onBackground),
            body1 = MaterialTheme.typography.body1.copy(colors.onBackground),
            body2 = MaterialTheme.typography.body2.copy(colors.onBackground),
            button = MaterialTheme.typography.button.copy(colors.onPrimary),
            caption = MaterialTheme.typography.caption.copy(colors.onBackground),
            overline = MaterialTheme.typography.overline.copy(colors.onBackground)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .background(colors.background),
            content = content
        )
    }
}
package io.github.bommbomm34.intervirt.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.api.RemoteContainerSession
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.kdroidfilter.webview.web.WebView
import io.github.kdroidfilter.webview.web.rememberWebViewNavigator
import io.github.kdroidfilter.webview.web.rememberWebViewState
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.compose.koinInject

@Composable
fun ShellView(computer: ViewDevice.Computer) {
    val executor = koinInject<Executor>()
    val appState = koinInject<AppState>()
    val preferences = koinInject<Preferences>()
    val navigator = rememberWebViewNavigator()
    val logger = remember { KotlinLogging.logger { } }
    var ready by remember { mutableStateOf(false) }
    if (preferences.ENABLE_AGENT) {
        var session: RemoteContainerSession? by remember { mutableStateOf(null) }
        LaunchedEffect(computer.id) {
            val result = executor.getContainerSession(computer.id)
            result.fold(
                onSuccess = {
                    session = it
                    ready = true
                },
                onFailure = {
                    appState.openDialog(
                        importance = Importance.ERROR,
                        message = it.localizedMessage
                    )
                }
            )
        }
    }

    Column {
        WebView(
            state = rememberWebViewState(""),
            modifier = Modifier.fillMaxSize(),
            navigator = navigator
        )
    }
}
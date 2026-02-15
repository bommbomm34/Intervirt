package io.github.bommbomm34.intervirt.webview

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
class WebViewNavigator(
    val coroutineScope: CoroutineScope
) {
    private sealed interface NavigationEvent {
        data object Back : NavigationEvent
        data object Forward : NavigationEvent
        data object Reload : NavigationEvent
        data object StopLoading : NavigationEvent

        data class LoadUrl(
            val url: String,
            val additionalHttpHeaders: Map<String, String> = emptyMap(),
        ) : NavigationEvent

        data class EvaluateJavaScript(
            val script: String,
            val callback: ((String) -> Unit)? = null
        ) : NavigationEvent
    }

    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow(replay = 1)

    internal suspend fun WebViewPanel.handleNavigationEvents(): Nothing =
        withContext(Dispatchers.Main) {
            navigationEvents.collect { event ->
                when (event) {
                    NavigationEvent.Back -> goBack()
                    NavigationEvent.Forward -> goForward()
                    NavigationEvent.Reload -> reload()
                    NavigationEvent.StopLoading -> stopLoading()
                    is NavigationEvent.LoadUrl -> {
                        loadUrl(event.url, event.additionalHttpHeaders)
                    }
                    is NavigationEvent.EvaluateJavaScript -> evaluateJavaScript(event.script, event.callback)
                }
            }
        }

    var canGoBack: Boolean by mutableStateOf(false)
        internal set

    var canGoForward: Boolean by mutableStateOf(false)
        internal set

    fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) {
        coroutineScope.launch {
            navigationEvents.emit(NavigationEvent.LoadUrl(url, additionalHttpHeaders))
        }
    }

    fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)? = null
    ) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.EvaluateJavaScript(script, callback)) }
    }

    fun navigateBack() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Back) }
    }

    fun navigateForward() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Forward) }
    }

    fun reload() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Reload) }
    }

    fun stopLoading() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.StopLoading) }
    }
}

@Composable
fun rememberWebViewNavigator(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): WebViewNavigator =
    remember(coroutineScope) { WebViewNavigator(coroutineScope) }

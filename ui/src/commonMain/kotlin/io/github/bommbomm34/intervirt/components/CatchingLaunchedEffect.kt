package io.github.bommbomm34.intervirt.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.bommbomm34.intervirt.data.AppState
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

@Composable
fun CatchingLaunchedEffect(
    key: Any? = Unit,
    block: suspend CoroutineScope.() -> Unit
) {
    val appState = koinInject<AppState>()
    LaunchedEffect(key) {
        appState.runDialogCatching { block() }
    }
}
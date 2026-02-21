package io.github.bommbomm34.intervirt.intervirtos.components

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.buttons.PlayButton
import io.github.bommbomm34.intervirt.core.api.SystemServiceManager
import io.github.bommbomm34.intervirt.data.AppState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SystemServiceView(
    serviceName: String,
    serviceManager: SystemServiceManager,
) {
    val scope = rememberCoroutineScope()
    val appState = koinInject<AppState>()
    var running by remember { mutableStateOf(false) }
    CatchingLaunchedEffect {
        running = serviceManager.status(serviceName).getOrThrow().active
    }
    PlayButton(running) {
        scope.launch {
            appState.runDialogCatching {
                if (it) serviceManager.start(serviceName).getOrThrow()
                else serviceManager.stop(serviceName).getOrThrow()
                running = it
            }
        }
    }
}
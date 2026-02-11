package io.github.bommbomm34.intervirt.gui.intervirtos.components

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.SystemServiceManager
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.PlayButton
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SystemServiceView(
    serviceName: String,
    serviceManager: SystemServiceManager
) {
    val scope = rememberCoroutineScope()
    val appState = koinInject<AppState>()
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appState.runDialogCatching {
            running = serviceManager.status(serviceName).getOrThrow().active
        }
    }
    AlignedBox(Alignment.TopEnd) {
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
}
package io.github.bommbomm34.intervirt.intervirtos.components

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.components.buttons.PlayButton
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.data.AppState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun DockerContainerView(
    name: String,
    dockerManager: DockerManager,
) {
    val scope = rememberCoroutineScope()
    val appState = koinInject<AppState>()
    var id: String? by remember { mutableStateOf(null) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appState.runDialogCatching {
            val newId = dockerManager
                .getContainer(name)
                .getOrThrow()
            require(newId != null) { "Container $name doesn't exist" }
            id = newId
            running = dockerManager
                .isContainerRunning(newId)
                .getOrThrow()
        }
    }
    id?.let { idClone ->
        PlayButton(running) {
            scope.launch {
                appState.runDialogCatching {
                    if (it) dockerManager.startContainer(idClone).getOrThrow()
                    else dockerManager.stopContainer(idClone).getOrThrow()
                    running = it
                }
            }
        }
    }
}
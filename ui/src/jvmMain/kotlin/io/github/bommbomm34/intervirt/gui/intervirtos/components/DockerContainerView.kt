package io.github.bommbomm34.intervirt.gui.intervirtos.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.buttons.PlayButton
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun DockerContainerView(
    id: String,
    dockerManager: DockerManager
) {
    val scope = rememberCoroutineScope()
    val appState = koinInject<AppState>()
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appState.runDialogCatching {
            running = dockerManager.isContainerRunning(id).getOrThrow()
        }
    }
    PlayButton(running) {
        scope.launch {
            appState.runDialogCatching {
                if (it) dockerManager.startContainer(id).getOrThrow()
                else dockerManager.stopContainer(id).getOrThrow()
                running = it
            }
        }
    }
}
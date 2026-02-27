package io.github.bommbomm34.intervirt.intervirtos.model.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.data.AppState
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class DockerContainerViewModel(
    @InjectedParam val name: String,
    @InjectedParam val dockerManager: DockerManager,
    private val appState: AppState,
) : ViewModel() {
    var id: String? by mutableStateOf(null)
    var running by mutableStateOf(false)

    init {
        viewModelScope.launchDialogCatching(appState) {
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

    fun enable(enabled: Boolean) {
        viewModelScope.launchDialogCatching(appState) {
            if (enabled) dockerManager.startContainer(id!!).getOrThrow()
            else dockerManager.stopContainer(id!!).getOrThrow()
            running = enabled
        }
    }
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

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
                
            require(newId != null) { "Container $name doesn't exist" }
            id = newId
            running = dockerManager
                .isContainerRunning(newId)
                
        }
    }

    fun enable(enabled: Boolean) {
        viewModelScope.launchDialogCatching(appState) {
            if (enabled) dockerManager.startContainer(id!!)
            else dockerManager.stopContainer(id!!)
            running = enabled
        }
    }
}

package io.github.bommbomm34.intervirt.intervirtos.model.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.SystemServiceManager
import io.github.bommbomm34.intervirt.data.AppState
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SystemServiceViewModel(
    @InjectedParam val serviceName: String,
    @InjectedParam val serviceManager: SystemServiceManager,
    private val appState: AppState,
) : ViewModel() {
    var running by mutableStateOf(false)

    fun enable(enabled: Boolean) {
        viewModelScope.launchDialogCatching(appState) {
            if (enabled) serviceManager.start(serviceName).getOrThrow()
            else serviceManager.stop(serviceName).getOrThrow()
            running = enabled
        }
    }
}
package io.github.bommbomm34.intervirt.model.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.AppState
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class VMManagerViewModel(
    private val qemuClient: QemuClient,
    private val appState: AppState,
    private val configuration: IntervirtConfiguration,
    private val guestManager: GuestManager,
) : ViewModel() {
    var running by mutableStateOf(false)

    init {
        viewModelScope.launch {
            qemuClient.onRunningChange { running = it }
        }
    }

    fun shutdown() {
        viewModelScope.launch {
            qemuClient.shutdownAlpine()
        }
    }

    fun reboot() {
        viewModelScope.launchDialogCatching(appState) {
            qemuClient.shutdownAlpine()
            qemuClient.bootAlpine().getOrThrow()
        }
    }

    fun sync() {
        appState.openDialog {
            ProgressDialog(
                flow = configuration.syncConfiguration(guestManager),
                onClose = ::close,
            )
        }
    }
}
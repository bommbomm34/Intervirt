package io.github.bommbomm34.intervirt.model.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.data.AppState
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class UpdaterViewModel(
    private val downloader: Downloader,
    private val appState: AppState,
    @InjectedParam val onClose: () -> Unit,
) : ViewModel() {
    val updates = mutableStateListOf<Downloader.Component>()
    val applyUpdates = mutableStateListOf<Downloader.Component>()

    init {
        viewModelScope.launchDialogCatching(appState){
            updates.addAll(downloader.checkUpdates().getOrThrow())
        }
    }

    fun update(){
        viewModelScope.launchDialogCatching(appState) {
            appState.openDialog {
                ProgressDialog(
                    flow = downloader.upgrade(applyUpdates),
                    onMessage = {
                        if (it is ResultProgress.Result<String>) onClose()
                    },
                    onClose = ::close,
                )
            }
        }
    }
}
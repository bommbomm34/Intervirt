package io.github.bommbomm34.intervirt.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bommbomm34.intervirt.HELP_URL
import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.syncConfiguration
import io.github.bommbomm34.intervirt.core.roundBy
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.UpdaterState
import io.github.bommbomm34.intervirt.home.Updater
import io.github.bommbomm34.intervirt.loadConf
import io.github.bommbomm34.intervirt.writeConf
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import java.awt.Desktop
import java.net.URI

@KoinViewModel
class HomeViewModel(
    private val appState: AppState,
    private val appEnv: AppEnv,
    private val guestManager: GuestManager,
    private val configuration: IntervirtConfiguration,
    private val downloader: Downloader,
    private val qemuClient: QemuClient,
) : ViewModel() {
    var devicesViewRenderKey by mutableStateOf(0)
    var showOptions by mutableStateOf(false)
    val updaterState = UpdaterState()
    var vmRunning by mutableStateOf(false)

    init {
        qemuClient.onRunningChange { vmRunning = it }
    }

    fun onConfChange() {
        devicesViewRenderKey++
    }

    fun getZoom() = "${appState.devicesViewZoom.roundBy(1)}x"

    fun open() {
        viewModelScope.launch {
            val file = FileKit.openFilePicker(
                type = FileKitType.File(extensions = listOf("ivrt")),
            )
            file?.file?.loadConf(configuration, appState, guestManager){
                onConfChange()
            }
        }
        onDismiss()
    }

    fun save() {
        viewModelScope.launch {
            val file = FileKit.openFileSaver(
                suggestedName = appEnv.SUGGESTED_FILENAME,
                extension = "ivrt",
            )
            file?.file?.writeConf(configuration)
        }
        onDismiss()
    }

    fun saveAs() {
        val file = appState.currentFile
        if (file != null) {
            viewModelScope.launch { file.file.writeConf(configuration) }
            onDismiss()
        } else save()
    }

    fun update() {
        appState.openDialog {
            Updater(
                state = updaterState,
                onUpdate = ::onUpdate,
            )
        }
    }

    fun openSettings() {
        appState.currentScreenIndex = 2
    }

    fun openAbout() {
        appState.currentScreenIndex = 3
    }

    fun openHelp() {
        Desktop.getDesktop().browse(URI(HELP_URL))
        onDismiss()
    }

    fun onDismiss() {
        showOptions = false
    }

    fun onUpdate() {
        viewModelScope.launchDialogCatching(appState) {
            appState.openDialog {
                ProgressDialog(
                    flow = downloader.upgrade(updaterState.applyUpdates),
                    onMessage = {
                        if (it is ResultProgress.Result<String>) close()
                    },
                    onClose = ::close,
                )
            }
        }
    }

    fun boot() {
        viewModelScope.launchDialogCatching(appState) {
            if (vmRunning) {
                qemuClient.shutdownAlpine()
            } else {
                qemuClient.bootAlpine().getOrThrow()
            }
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
                flow = guestManager.syncConfiguration(configuration),
                onClose = ::close,
            )
        }
    }
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.syncProject
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.bommbomm34.intervirt.core.util.ext.roundBy
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Screen
import io.github.bommbomm34.intervirt.data.UpdaterState
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.home.Updater
import io.github.bommbomm34.intervirt.util.ext.loadConf
import io.github.bommbomm34.intervirt.util.ext.writeConf
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import java.awt.Desktop
import java.net.URI

@KoinViewModel
class HomeViewModel(
    private val appState: AppState,
    private val appEnv: AppEnv,
    private val guestManager: GuestManager,
    private val downloader: Downloader,
    private val qemuClient: QemuClient,
    private val _project: Atomic<Project>,
) : ViewModel() {
    var devicesViewRenderKey by mutableIntStateOf(0)
    var showOptions by mutableStateOf(false)
    val updaterState = UpdaterState()
    var vmRunning by mutableStateOf(false)
    var project by _project

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
            file?.let {
                it.loadConf(_project, appState, guestManager) {
                    onConfChange()
                }
                appState.currentFile = it
            }
        }
        onDismiss()
    }

    fun save() {
        val file = appState.currentFile
        if (file != null) {
            viewModelScope.launch { file.writeConf(project) }
            onDismiss()
        } else saveAs()
    }

    fun saveAs() {
        viewModelScope.launch {
            val file = FileKit.openFileSaver(
                suggestedName = appEnv.suggestedFilename,
                extension = "ivrt",
            )
            file?.let {
                it.writeConf(project)
                appState.currentFile = it
            }
        }
        onDismiss()
    }

    fun update() {
        appState.openDialog {
            Updater(
                state = updaterState,
                onUpdate = ::onUpdate,
            )
        }
        onDismiss()
    }

    fun openSettings() {
        appState.currentScreen = Screen.SETTINGS
        onDismiss()
    }

    fun openAbout() {
        appState.currentScreen = Screen.ABOUT
        onDismiss()
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
                qemuClient.bootAlpine()
            }
        }
    }

    fun reboot() {
        viewModelScope.launchDialogCatching(appState) {
            qemuClient.shutdownAlpine()
            qemuClient.bootAlpine()
        }
    }

    fun sync() {
        appState.openDialog {
            ProgressDialog(
                flow = guestManager.syncProject(project),
                onClose = ::close,
            )
        }
    }
}

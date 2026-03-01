package io.github.bommbomm34.intervirt.model.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bommbomm34.intervirt.HELP_URL
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.home.Updater
import io.github.bommbomm34.intervirt.loadConf
import io.github.bommbomm34.intervirt.writeConf
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import java.awt.Desktop
import java.net.URI

@KoinViewModel
class OptionDropdownViewModel(
    private val appState: AppState,
    private val appEnv: AppEnv,
    private val guestManager: GuestManager,
    private val configuration: IntervirtConfiguration,
    @InjectedParam private val onConfChange: () -> Unit,
    @InjectedParam private val onDismiss: () -> Unit,
) : ViewModel() {
    private val logger = KotlinLogging.logger { }

    fun open() {
        viewModelScope.launch {
            val file = FileKit.openFilePicker(
                type = FileKitType.File(extensions = listOf("ivrt")),
            )
            if (file != null) {
                file.file.loadConf(configuration, appState, guestManager)
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
            Updater(::close)
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
}
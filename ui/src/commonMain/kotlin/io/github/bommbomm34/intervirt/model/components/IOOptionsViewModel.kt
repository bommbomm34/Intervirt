package io.github.bommbomm34.intervirt.model.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.components.filepicker.ContainerFilePicker
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.name

@KoinViewModel
class IOOptionsViewModel(
    private val appState: AppState,
    private val deviceManager: DeviceManager,
    @InjectedParam val device: ViewDevice.Computer
) : ViewModel() {
    private var containerFilePath: Path? by mutableStateOf(null)
    var ioClient: ContainerIOClient? by mutableStateOf(null)
    private val logger = KotlinLogging.logger { }

    init {
        viewModelScope.launchDialogCatching(appState) {
            ioClient = deviceManager.getIOClient(device.device).getOrThrow()
        }
    }

    fun download(){
        logger.debug { "Downloading file from ${device.id}" }
        appState.openDialog(width = 1000.dp, height = 800.dp) {
            ContainerFilePicker(
                ioClient!!,
            ) { path ->
                close()
                path?.let {
                    containerFilePath = it
                    val fullFileName = path.name
                    viewModelScope.launch {
                        val file = FileKit.openFileSaver(
                            suggestedName = fullFileName.substringBefore("."),
                            extension = fullFileName.substringAfterLast("."),
                        )
                        file?.let {
                            viewModelScope.launchDialogCatching(appState) {
                                containerFilePath!!.copyTo(file.file.toPath(), overwrite = true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun upload(){
        logger.debug { "Uploading file to ${device.id}" }
        viewModelScope.launch {
            val file = FileKit.openFilePicker()
            file?.let { _ ->
                appState.openDialog(width = 1000.dp, height = 800.dp) {
                    val scope = rememberCoroutineScope()
                    ContainerFilePicker(
                        ioClient!!,
                        file.name,
                    ) { path ->
                        close()
                        path?.let { _ ->
                            scope.launchDialogCatching(appState) {
                                file.file.toPath().copyTo(path, true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun openShell(){
        appState.openComputerShell = device
    }
}
package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import compose.icons.TablerIcons
import compose.icons.tablericons.FileDownload
import compose.icons.tablericons.FileUpload
import compose.icons.tablericons.Terminal
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.download_file
import intervirt.composeapp.generated.resources.terminal
import intervirt.composeapp.generated.resources.upload_file
import io.github.bommbomm34.intervirt.api.ContainerIOClient
import io.github.bommbomm34.intervirt.api.impl.ContainerSshClient
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.api.FileManager
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.ContainerFilePicker
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.rememberLogger
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.name

@Composable
fun IOOptions(device: ViewDevice.Computer){
    val scope = rememberCoroutineScope()
    val appState = koinInject<AppState>()
    val deviceManager = koinInject<DeviceManager>()
    val logger = rememberLogger("IOOptions")
    var ioClient: ContainerIOClient? by remember { mutableStateOf(null) }
    var containerFilePath: Path? by remember { mutableStateOf(null) }
    LaunchedEffect(device.id){
        ioClient = deviceManager.getIOClient(device.device).getOrElse {
            logger.error(it) { "Failure during obtaining a IOClient for ${device.id}" }
            null
        }
    }
    val fileSaverLauncher = rememberFileSaverLauncher { file ->
        file?.let {
            scope.launch {
                // containerFilePath must be valid if this launcher is called
                try {
                    containerFilePath!!.copyTo(file.file.toPath())
                } catch (e: Exception){
                    appState.openDialog(
                        importance = Importance.ERROR,
                        message = e.localizedMessage
                    )
                }
            }
        }
    }
    val filePickerLauncher = rememberFilePickerLauncher { file ->
        file?.let { _ ->
            appState.openDialog {
                ContainerFilePicker(device){ path ->
                    path?.let { _ ->
                        scope.launch {
                            try {
                                file.file.toPath().copyTo(path)
                            } catch (e: Exception){
                                appState.openDialog(
                                    importance = Importance.ERROR,
                                    message = e.localizedMessage
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    Row (verticalAlignment = Alignment.CenterVertically) {
        ioClient?.let {
            IconButton(
                onClick = {
                    appState.openDialog {
                        ContainerFilePicker(device){ path ->
                            path?.let {
                                containerFilePath = it
                                val fullFileName = path.name
                                fileSaverLauncher.launch(
                                    suggestedName = fullFileName.substringBefore("."),
                                    extension = fullFileName.substringAfterLast(".")
                                )
                            }
                        }
                    }
                }
            ){
                GeneralIcon(
                    imageVector = TablerIcons.FileUpload,
                    contentDescription = stringResource(Res.string.upload_file)
                )
            }
            GeneralSpacer()
            IconButton(
                onClick = {
                    filePickerLauncher.launch()
                }
            ){
                GeneralIcon(
                    imageVector = TablerIcons.FileDownload,
                    contentDescription = stringResource(Res.string.download_file)
                )
            }
            GeneralSpacer()
        }
        IconButton(
            onClick = {
                appState.openComputerShell = device
            }
        ){
            GeneralIcon(
                imageVector = TablerIcons.Terminal,
                contentDescription =  stringResource(Res.string.terminal)
            )
        }
    }
}
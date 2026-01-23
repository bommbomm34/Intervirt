package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import compose.icons.TablerIcons
import compose.icons.tablericons.FileDownload
import compose.icons.tablericons.FileUpload
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.download_file
import intervirt.composeapp.generated.resources.upload_file
import io.github.bommbomm34.intervirt.api.AgentClient
import io.github.bommbomm34.intervirt.api.FileManager
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.ContainerFilePicker
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.openDialog
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun FileTransferOptions(device: ViewDevice.Computer){
    val scope = rememberCoroutineScope()
    val fileManager = koinInject<FileManager>()
    var containerFilePath by remember { mutableStateOf("") }
    val fileSaverLauncher = rememberFileSaverLauncher { file ->
        file?.let {
            scope.launch {
                // containerFilePath must be valid if this launcher is called
                fileManager.pullFile(
                    device = device.device,
                    path = containerFilePath,
                    destFile = it
                )
            }
        }
    }
    val filePickerLauncher = rememberFilePickerLauncher { file ->
        file?.let { _ ->
            openDialog {
                ContainerFilePicker(device){ path ->
                    path?.let { _ ->
                        scope.launch {
                            fileManager.pushFile(
                                device = device.device,
                                path = path,
                                platformFile = file
                            ).collect {
                                // TODO: Show progress via dialog
                            }
                        }
                    }
                }
            }
        }
    }
    Row (verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {
                openDialog {
                    ContainerFilePicker(device){ path ->
                        path?.let {
                            containerFilePath = it
                            val fullFileName = path.substringAfterLast("/")
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
    }
}
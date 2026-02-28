package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.download_file
import intervirt.ui.generated.resources.terminal
import intervirt.ui.generated.resources.upload_file
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.components.filepicker.ContainerFilePicker
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.rememberFileSaverLauncher
import io.github.bommbomm34.intervirt.rememberLogger
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.name

@Composable
fun IOOptions(device: ViewDevice.Computer) {
    val scope = rememberCoroutineScope()
    val appState = koinInject<AppState>()
    val deviceManager = koinInject<DeviceManager>()
    val logger = rememberLogger("IOOptions")
    var ioClient: ContainerIOClient? by remember { mutableStateOf(null) }
    var containerFilePath: Path? by remember { mutableStateOf(null) }
    CatchingLaunchedEffect(device) {
        ioClient = deviceManager.getIOClient(device.device).getOrThrow()
    }
    val fileSaverLauncher = rememberFileSaverLauncher { file ->
        file?.let {
            scope.launchDialogCatching(appState) {
                // containerFilePath must be valid if this launcher is called
                containerFilePath!!.copyTo(file.file.toPath(), overwrite = true)
            }
        }
    }
    val filePickerLauncher = rememberFilePickerLauncher { file ->
        file?.let { _ ->
            appState.openDialog(width = 1000.dp, height = 800.dp) {
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
    ioClient?.let { client ->
        IconButton(
            onClick = {
                logger.debug { "Downloading file from ${device.id}" }
                appState.openDialog(width = 1000.dp, height = 800.dp) {
                    ContainerFilePicker(
                        client,
                    ) { path ->
                        close()
                        path?.let {
                            containerFilePath = it
                            val fullFileName = path.name
                            fileSaverLauncher.launch(
                                suggestedName = fullFileName.substringBefore("."),
                                extension = fullFileName.substringAfterLast("."),
                            )
                        }
                    }
                }
            },
        ) {
            GeneralIcon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = stringResource(Res.string.download_file),
            )
        }
        GeneralSpacer()
        IconButton(
            onClick = {
                logger.debug { "Uploading file to ${device.id}" }
                filePickerLauncher.launch()
            },
        ) {
            GeneralIcon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = stringResource(Res.string.upload_file),
            )
        }
        GeneralSpacer()
    }
    IconButton(
        onClick = {
            appState.openComputerShell = device
        },
    ) {
        GeneralIcon(
            imageVector = Icons.Default.Terminal,
            contentDescription = stringResource(Res.string.terminal),
        )
    }
}
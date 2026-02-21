package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog
import io.github.bommbomm34.intervirt.components.filepicker.ContainerFilePicker
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.rememberLogger
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
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
    LaunchedEffect(device.id) {
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
                } catch (e: Exception) {
                    appState.openDialog(
                        severity = Severity.ERROR,
                        message = e.localizedMessage,
                    )
                }
            }
        }
    }
    val filePickerLauncher = rememberFilePickerLauncher { file ->
        file?.let { _ ->
            appState.openDialog {
                ContainerFilePicker(
                    ioClient!!,
                    file.name,
                ) { path ->
                    close()
                    path?.let { _ ->
                        scope.launch {
                            appState.runDialogCatching {
                                if (file.exists()) {
                                    appState.openDialog {
                                        AcceptDialog(
                                            message = stringResource(Res.string.file_already_exists),
                                            onCancel = ::close,
                                        ) {
                                            close()
                                            file.file.toPath().copyTo(path, true)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        ioClient?.let { client ->
            IconButton(
                onClick = {
                    appState.openDialog {
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
}
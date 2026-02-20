package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import compose.icons.TablerIcons
import compose.icons.tablericons.FileDownload
import compose.icons.tablericons.FileUpload
import compose.icons.tablericons.Terminal
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.dialogs.AcceptDialog
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.filepicker.ContainerFilePicker
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
                _root_ide_package_.io.github.bommbomm34.intervirt.components.filepicker.ContainerFilePicker(
                    ioClient!!,
                    file.name
                ) { path ->
                    close()
                    path?.let { _ ->
                        scope.launch {
                            appState.runDialogCatching {
                                if (file.exists()) {
                                    appState.openDialog {
                                        _root_ide_package_.io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog(
                                            message = stringResource(Res.string.file_already_exists),
                                        ) { file.file.toPath().copyTo(path, true) }
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
                        _root_ide_package_.io.github.bommbomm34.intervirt.components.filepicker.ContainerFilePicker(
                            client
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
                _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralIcon(
                    imageVector = TablerIcons.FileDownload,
                    contentDescription = stringResource(Res.string.download_file),
                )
            }
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            IconButton(
                onClick = {
                    filePickerLauncher.launch()
                },
            ) {
                _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralIcon(
                    imageVector = TablerIcons.FileUpload,
                    contentDescription = stringResource(Res.string.upload_file),
                )
            }
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        }
        IconButton(
            onClick = {
                appState.openComputerShell = device
            },
        ) {
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralIcon(
                imageVector = TablerIcons.Terminal,
                contentDescription = stringResource(Res.string.terminal),
            )
        }
    }
}
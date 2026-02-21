package io.github.bommbomm34.intervirt.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.HELP_URL
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewConfiguration
import io.github.bommbomm34.intervirt.components.buttons.IconText
import io.github.bommbomm34.intervirt.rememberLogger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.awt.Desktop
import java.net.URI

@Composable
fun OptionDropdown(
    expanded: Boolean,
    onConfChange: () -> Unit,
    onDismiss: () -> Unit,
) {
    val logger = rememberLogger("OptionDropdown")
    val appEnv = koinInject<AppEnv>()
    val guestManager = koinInject<GuestManager>()
    val appState = koinInject<AppState>()
    val configuration = koinInject<IntervirtConfiguration>()
    val scope = rememberCoroutineScope()
    val writeConf: (PlatformFile) -> Unit = { file ->
        scope.launch {
            file.writeString(Json.encodeToString(configuration))
        }
    }
    val fileSaverLauncher = rememberFileSaverLauncher { file ->
        if (file != null) writeConf(file)
    }
    val filePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("ivrt")),
    ) { file ->
        if (file != null) {
            scope.launch(Dispatchers.IO) {
                val fileContent = file.readString()
                val newConfiguration = Json.decodeFromString<IntervirtConfiguration>(fileContent)
                configuration.update(newConfiguration)
                if (appEnv.ENABLE_AGENT) {
                    configuration.syncConfiguration(guestManager).collect {
                        logger.info { it }
                    }
                }
                appState.statefulConf.update(ViewConfiguration(newConfiguration))
                onConfChange()
            }
        }
    }
    Column {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
        ) {
            // Open
            DropdownMenuItem(
                onClick = {
                    filePickerLauncher.launch()
                    onDismiss()
                },
                text = {
                    IconText(
                        imageVector = TablerIcons.Folder,
                        text = stringResource(Res.string.open),
                    )
                }
            )
            // Save
            DropdownMenuItem(
                onClick = {
                    val file = appState.currentFile // Copy delegated state variable
                    if (file != null) writeConf(file) else fileSaverLauncher.launch(
                        suggestedName = appEnv.SUGGESTED_FILENAME,
                        extension = "ivrt",
                    )
                    onDismiss()
                },
                text = {
                    IconText(
                        imageVector = TablerIcons.DeviceFloppy,
                        text = stringResource(Res.string.save),
                    )
                }
            )
            // Save As
            DropdownMenuItem(
                onClick = {
                    fileSaverLauncher.launch(
                        suggestedName = appEnv.SUGGESTED_FILENAME,
                        extension = "ivrt",
                    )
                    onDismiss()
                },
                text = {
                    IconText(
                        imageVector = TablerIcons.DeviceFloppy,
                        text = stringResource(Res.string.save_as),
                    )
                }
            )
            // Update
            DropdownMenuItem(
                onClick = {
                    appState.openDialog {
                        Updater(::close)
                    }
                    onDismiss()
                },
                text = {
                    IconText(
                        imageVector = TablerIcons.Refresh,
                        text = stringResource(Res.string.update),
                    )
                }
            )
            // Settings
            DropdownMenuItem(
                onClick = { appState.currentScreenIndex = 2 },
                text = {
                    IconText(
                        imageVector = TablerIcons.Settings,
                        text = stringResource(Res.string.settings),
                    )
                }
            )
            // About
            DropdownMenuItem(
                onClick = { appState.currentScreenIndex = 3 },
                text = {
                    IconText(
                        imageVector = TablerIcons.InfoCircle,
                        text = stringResource(Res.string.about),
                    )
                }
            )
            // Help
            DropdownMenuItem(
                onClick = {
                    Desktop.getDesktop().browse(URI(HELP_URL))
                    onDismiss()
                },
                text = {
                    IconText(
                        imageVector = TablerIcons.Help,
                        text = stringResource(Res.string.help),
                    )
                }
            )
        }
    }
}
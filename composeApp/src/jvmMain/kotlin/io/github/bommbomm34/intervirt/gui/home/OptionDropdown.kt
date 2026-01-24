package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.*
import io.github.bommbomm34.intervirt.api.AgentClient
import io.github.bommbomm34.intervirt.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.data.stateful.ViewConfiguration
import io.github.bommbomm34.intervirt.gui.components.buttons.IconText
import io.github.oshai.kotlinlogging.KotlinLogging
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
    onDismiss: () -> Unit
) {
    val logger = KotlinLogging.logger {  }
    val preferences = koinInject<Preferences>()
    val agentClient = koinInject<AgentClient>()
    val appState = koinInject<AppState>()
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val writeConf: (PlatformFile) -> Unit = { file ->
        scope.launch {
            file.writeString(Json.encodeToString(configuration))
        }
    }
    val fileSaverLauncher = rememberFileSaverLauncher { file ->
        if (file != null) writeConf(file)
    }
    val filePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("ivrt"))
    ) { file ->
        if (file != null){
            scope.launch {
                val fileContent = file.readString()
                val newConfiguration = Json.decodeFromString<IntervirtConfiguration>(fileContent)
                configuration.update(newConfiguration)
                if (preferences.ENABLE_AGENT) {
                    configuration.syncConfiguration(agentClient).collect {
                        logger.info { it }
                    }
                }
                appState.statefulConf.update(ViewConfiguration(newConfiguration))
            }
        }
    }
    Column {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ) {
            // Open
            DropdownMenuItem(
                onClick = {
                    filePickerLauncher.launch()
                    onDismiss()
                }
            ){
                IconText(
                    imageVector = TablerIcons.Folder,
                    text = stringResource(Res.string.open)
                )
            }
            // Save
            DropdownMenuItem(
                onClick = {
                    val file = appState.currentFile // Copy delegated state variable
                    if (file != null) writeConf(file) else fileSaverLauncher.launch(
                        suggestedName = preferences.SUGGESTED_FILENAME,
                        extension = "ivrt"
                    )
                    onDismiss()
                }
            ) {
                IconText(
                    imageVector = TablerIcons.DeviceFloppy,
                    text = stringResource(Res.string.save)
                )
            }
            // Save As
            DropdownMenuItem(
                onClick = {
                    fileSaverLauncher.launch(
                        suggestedName = preferences.SUGGESTED_FILENAME,
                        extension = "ivrt"
                    )
                    onDismiss()
                }
            ) {
                IconText(
                    imageVector = TablerIcons.DeviceFloppy,
                    text = stringResource(Res.string.save_as)
                )
            }
            // Settings
            DropdownMenuItem(
                onClick = { appState.currentScreenIndex = 2 }
            ) {
                IconText(
                    imageVector = TablerIcons.Settings,
                    text = stringResource(Res.string.settings)
                )
            }
            // About
            DropdownMenuItem(
                onClick = { appState.currentScreenIndex = 3 }
            ) {
                IconText(
                    imageVector = TablerIcons.InfoCircle,
                    text = stringResource(Res.string.about)
                )
            }
            // Help
            DropdownMenuItem(
                onClick = {
                    Desktop.getDesktop().browse(URI(HELP_URL))
                    onDismiss()
                }
            ){
                IconText(
                    imageVector = TablerIcons.Help,
                    text = stringResource(Res.string.help)
                )
            }
        }
    }
}
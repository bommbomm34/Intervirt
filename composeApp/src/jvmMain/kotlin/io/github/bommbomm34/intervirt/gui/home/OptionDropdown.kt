package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.Folder
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Settings
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.CURRENT_FILE
import io.github.bommbomm34.intervirt.SUGGESTED_FILENAME
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.currentScreenIndex
import io.github.bommbomm34.intervirt.data.FileManager
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.stateful.ViewConfiguration
import io.github.bommbomm34.intervirt.gui.components.buttons.IconText
import io.github.bommbomm34.intervirt.openDialog
import io.github.bommbomm34.intervirt.statefulConf
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource

@Composable
fun OptionDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit
) {
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
        type = FileKitType.File(extensions = listOf("ivrt"))
    ) { file ->
        if (file != null){
            scope.launch {
                val fileContent = file.readString()
                val newConfiguration = Json.decodeFromString<IntervirtConfiguration>(fileContent)
                configuration.update(newConfiguration)
                statefulConf.update(ViewConfiguration(newConfiguration))
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
                    val file = CURRENT_FILE // Copy delegated state variable
                    if (file != null) writeConf(file) else fileSaverLauncher.launch(
                        suggestedName = SUGGESTED_FILENAME,
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
                        suggestedName = SUGGESTED_FILENAME,
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
                onClick = { currentScreenIndex = 2 }
            ) {
                IconText(
                    imageVector = TablerIcons.Settings,
                    text = stringResource(Res.string.settings)
                )
            }
            // About
            DropdownMenuItem(
                onClick = { currentScreenIndex = 3 }
            ) {
                IconText(
                    imageVector = TablerIcons.InfoCircle,
                    text = stringResource(Res.string.about)
                )
            }
        }
    }
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.filepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterRow
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.BackButton
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.rememberLogger
import io.github.vinceglb.filekit.PlatformFile
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory

@Composable
fun ContainerFilePicker(
    ioClient: ContainerIOClient,
    saveFilename: String? = null, // If provided, a save dialog will be shown
    onPick: (Path?) -> Unit,
) {
    var currentPath by remember { mutableStateOf(ioClient.getPath("/")) }
    val files = currentPath.toFile()
        .listFiles()
        .map { it.toPath() }
    val logger = rememberLogger("ContainerFilePicker")
    Column(
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .fillMaxWidth(0.9f),
    ) {
        CenterRow {
            CloseButton {
                logger.debug { "Selected nothing" }
                onPick(null)
            }
            GeneralSpacer()
            currentPath.parent?.let {
                BackButton {
                    currentPath = it
                }
            }
        }
        GeneralSpacer()
        FilesTable(
            files = files,
            selectable = saveFilename == null,
        ) {
            if (it.isDirectory()) currentPath = it else {
                logger.debug { "Selected ${it.absolutePathString()}" }
                onPick(it)
            }
        }
        saveFilename?.let { default ->
            AlignedBox(Alignment.BottomCenter) {
                FileSaveView(default) { onPick(currentPath.resolve(it)) }
            }
        }
    }
}
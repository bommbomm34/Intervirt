/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.filepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.current_directory
import io.github.bommbomm34.intervirt.appendResource
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterRow
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.BackButton
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.util.ext.toJavaPath
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.listFiles
import io.github.bommbomm34.intervirt.rememberLogger
import org.koin.compose.koinInject
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory

private val BOLD_STYLE = SpanStyle(fontWeight = FontWeight.Bold)

@Composable
fun ContainerFilePicker(
    ioClient: ContainerIOClient,
    saveFilename: String? = null, // If provided, a save dialog will be shown
    onPick: (Path?) -> Unit,
) {
    var currentPath by remember { mutableStateOf(ioClient.getPath("/")) }
    val files = currentPath.listFiles()
    val logger = rememberLogger("ContainerFilePicker")
    val fileManager = koinInject<FileManager>()

    Column(
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .fillMaxWidth(0.9f),
    ) {
        CenterRow {
            currentPath.parent?.let {
                if (shouldShowBackButton(it, fileManager, currentAppEnv.virtualContainerIO)) {
                    BackButton {
                        currentPath = it
                    }
                }
            }
            GeneralSpacer()
            Text(
                text = buildAnnotatedString {
                    withStyle(BOLD_STYLE) {
                        appendResource(Res.string.current_directory)
                    }

                    append(currentPath.toString())
                },
            )
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
    }

    saveFilename?.let { default ->
        AlignedBox(Alignment.BottomCenter) {
            FileSaveView(default) { onPick(currentPath.resolve(it)) }
        }
    }
}

// It doesn't need to be highly secure because INTERVIRT_VIRTUAL_CONTAINER_IO
// should only be enabled during testing and not in production mode.
private fun shouldShowBackButton(
    parent: Path,
    fileManager: FileManager,
    virtualContainerIO: Boolean,
): Boolean {
    if (!virtualContainerIO) return true
    val virtualDir = fileManager.getFile("virtual").toJavaPath()
    if (parent == virtualDir) return false
    var parent: Path? = parent.parent

    while (parent != null) {
        if (parent == virtualDir) return true
        parent = parent.parent
    }

    return false
}

package io.github.bommbomm34.intervirt.gui.components.filepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.api.ContainerIOClient
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.BackButton
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.rememberLogger
import java.nio.file.Path

@Composable
fun ContainerFilePicker(
    ioClient: ContainerIOClient,
    saveFilename: String? = null, // If provided, a save dialog will be shown
    onPick: (Path?) -> Unit
) {
    var currentPath by remember { mutableStateOf(ioClient.getPath("/")) }
    val files = currentPath.toFile().listFiles().toList()
    val logger = rememberLogger("ContainerFilePicker")
    Column {
        Row {
            CloseButton {
                logger.debug { "Selected nothing" }
                onPick(null)
            }
            GeneralSpacer()
            currentPath.parent?.let { BackButton { currentPath = it } }
        }
        FilesTable(files) {
            if (it.isDirectory) currentPath = it.toPath() else {
                logger.debug { "Selected ${it.absolutePath}" }
                onPick(it.toPath())
            }
        }
        saveFilename?.let { default ->
            FileSaveView(default) { onPick(currentPath.resolve(it)) }
        }
    }
}
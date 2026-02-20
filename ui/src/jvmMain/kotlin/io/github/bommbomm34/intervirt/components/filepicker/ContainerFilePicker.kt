package io.github.bommbomm34.intervirt.components.filepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterRow
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.BackButton
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.rememberLogger
import java.nio.file.Path

@Composable
fun ContainerFilePicker(
    ioClient: ContainerIOClient,
    saveFilename: String? = null, // If provided, a save dialog will be shown
    onPick: (Path?) -> Unit,
) {
    var currentPath by remember { mutableStateOf(ioClient.getPath("/")) }
    val files = currentPath.toFile().listFiles().toList()
    val logger = rememberLogger("ContainerFilePicker")
    Column(
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .fillMaxWidth(0.9f),
    ) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterRow {
            _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.CloseButton {
                logger.debug { "Selected nothing" }
                onPick(null)
            }
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            currentPath.parent?.let {
                _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.BackButton {
                    currentPath = it
                }
            }
        }
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        FilesTable(
            files = files,
            selectable = saveFilename == null,
        ) {
            if (it.isDirectory) currentPath = it.toPath() else {
                logger.debug { "Selected ${it.absolutePath}" }
                onPick(it.toPath())
            }
        }
        saveFilename?.let { default ->
            _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.BottomCenter) {
                FileSaveView(default) { onPick(currentPath.resolve(it)) }
            }
        }
    }
}
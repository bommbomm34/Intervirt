package io.github.bommbomm34.intervirt.gui.components.filepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.ContainerIOClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.BackButton
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import java.nio.file.Path

@Composable
fun ContainerFilePicker(
    ioClient: ContainerIOClient,
    onPick: (Path?) -> Unit
) {
    var currentPath by remember { mutableStateOf(ioClient.getPath("/")) }
    val files = currentPath.toFile().listFiles().toList()
    println("Amount of files: ${files.size}")
    Column {
        Row {
            CloseButton { onPick(null) }
            GeneralSpacer()
            currentPath.parent?.let { BackButton { currentPath = it } }
        }
        FilesTable(files){
            if (it.isDirectory) currentPath = it.toPath() else onPick(it.toPath())
        }
    }
}
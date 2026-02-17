package io.github.bommbomm34.intervirt.gui.components.filepicker

import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.File
import compose.icons.tablericons.Folder
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.file
import intervirt.ui.generated.resources.folder
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.components.tables.ClickableTable
import io.github.bommbomm34.intervirt.gui.components.tables.VisibleText
import org.jetbrains.compose.resources.stringResource
import java.io.File

private val headers = listOf("", "Filename")

@Composable
fun FilesTable(
    files: List<File>,
    selectable: Boolean,
    onClick: (File) -> Unit
) = ClickableTable(
    headers = headers,
    data = files.map { file ->
        val isFile = file.isFile
        listOf(
            {
                // Icon
                GeneralIcon(
                    imageVector = if (isFile) TablerIcons.File else TablerIcons.Folder,
                    contentDescription = stringResource(if (isFile) Res.string.file else Res.string.folder)
                )
            },
            {
                // Filename
                VisibleText(file.name)
            }
        )
    }
) {
    val file = files[it]
    if (selectable || file.isDirectory) onClick(files[it])
}
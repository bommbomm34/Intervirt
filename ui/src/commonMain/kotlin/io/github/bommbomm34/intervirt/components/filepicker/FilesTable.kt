/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.filepicker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.file
import intervirt.ui.generated.resources.folder
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.tables.ClickableTable
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

private val headers = listOf("", "Filename")

@Composable
fun FilesTable(
    files: List<Path>,
    selectable: Boolean,
    onClick: (Path) -> Unit,
) = ClickableTable(
    headers = headers,
    data = files.map { file ->
        val isFile = file.isRegularFile()
        listOf(
            {
                // Icon
                GeneralIcon(
                    imageVector = if (isFile) Icons.Default.FilePresent else Icons.Default.Folder,
                    contentDescription = stringResource(if (isFile) Res.string.file else Res.string.folder),
                )
            },
            {
                // Filename
                Text(file.name)
            },
        )
    },
) {
    val file = files[it]
    if (selectable || file.isDirectory()) onClick(files[it])
}
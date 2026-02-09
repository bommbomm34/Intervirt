package io.github.bommbomm34.intervirt.gui.components.filepicker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.onClick
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import compose.icons.TablerIcons
import compose.icons.tablericons.File
import compose.icons.tablericons.Folder
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.file
import intervirt.composeapp.generated.resources.folder
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.components.VisibleText
import io.github.windedge.table.DataTable
import org.jetbrains.compose.resources.stringResource
import java.io.File

private val headers = listOf("", "Filename")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesTable(
    files: List<File>,
    onClick: (File) -> Unit
) {
    DataTable(
        columns = {
            headerBackground {
                Box(Modifier.background(MaterialTheme.colorScheme.onBackground))
            }
            headers.forEach {
                column { VisibleText(it, true) }
            }
        }
    ) {
        files.forEach {
            row(
                Modifier
                    .onClick {
                        onClick(it)
                    }
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                val isFile = it.isFile
                // File/Directory icon
                cell {
                    GeneralIcon(
                        imageVector = if (isFile) TablerIcons.File else TablerIcons.Folder,
                        contentDescription = stringResource(if (isFile) Res.string.file else Res.string.folder)
                    )
                }
                // Filename
                cell { VisibleText(it.name) }
            }
        }
    }
}
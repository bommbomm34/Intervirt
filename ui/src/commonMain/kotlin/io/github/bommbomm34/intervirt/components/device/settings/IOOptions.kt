/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.download_file
import intervirt.ui.generated.resources.os
import intervirt.ui.generated.resources.terminal
import intervirt.ui.generated.resources.upload_file
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.TooltipArea
import org.jetbrains.compose.resources.stringResource

@Composable
fun IOOptions(
    isIntervirtOS: Boolean,
    onDownload: () -> Unit,
    onUpload: () -> Unit,
    onOpenShell: () -> Unit,
) {
    TooltipArea(Res.string.download_file) {
        IconButton(
            onClick = onDownload,
        ) {
            GeneralIcon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = stringResource(Res.string.download_file),
            )
        }
    }
    GeneralSpacer()
    TooltipArea(Res.string.upload_file) {
        IconButton(
            onClick = onUpload,
        ) {
            GeneralIcon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = stringResource(Res.string.upload_file),
            )
        }
    }
    GeneralSpacer()
    TooltipArea(if (isIntervirtOS) Res.string.os else Res.string.terminal) {
        IconButton(
            onClick = onOpenShell,
        ) {
            GeneralIcon(
                imageVector = Icons.Default.Terminal,
                contentDescription = stringResource(Res.string.terminal),
            )
        }
    }
}

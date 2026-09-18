/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.filepicker

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.empty_directory
import intervirt.ui.generated.resources.upload_some_files
import io.github.bommbomm34.intervirt.appendResource
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import org.jetbrains.compose.resources.stringResource

private val COLOR = Color.LightGray.copy(alpha = 0.7f)
private val HEADLINE_STYLE = SpanStyle(fontSize = 32.sp)

@Composable
fun EmptyDirectoryView() {
    AlignedBox(Alignment.Center) {
        CenterColumn {
            val emptyDirectoryString = stringResource(Res.string.empty_directory)
            val emptyDirectoryDescription = buildAnnotatedString {
                withStyle(HEADLINE_STYLE) {
                    appendLine(emptyDirectoryString)
                }

                appendLine()
                appendResource(Res.string.upload_some_files)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Default.ListAlt,
                contentDescription = emptyDirectoryString,
                modifier = Modifier.size(64.dp),
                tint = COLOR,
            )
            GeneralSpacer()
            Text(
                text = emptyDirectoryDescription,
                textAlign = TextAlign.Center,
                color = COLOR,
            )
        }
    }
}

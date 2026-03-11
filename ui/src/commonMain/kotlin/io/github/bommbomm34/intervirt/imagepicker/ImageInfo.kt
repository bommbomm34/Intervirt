/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.install_os
import intervirt.ui.generated.resources.source_of_description
import intervirt.ui.generated.resources.source_of_icon
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.Overlay
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.data.Image
import org.jetbrains.compose.resources.stringResource

@Composable
fun ImageInfo(
    image: Image,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sourcesText = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
            append(stringResource(Res.string.source_of_description))
        }
        append(image.descriptionSource)
        append("\n")
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
            append(stringResource(Res.string.source_of_icon))
        }
        append(image.iconSource)
    }
    Overlay(0.8f) {
        AlignedBox(Alignment.TopStart) {
            ImageIcon(image)
        }
        AlignedBox(Alignment.TopCenter) {
            Text(
                text = image.toReadableName(),
                fontSize = 24.sp,
            )
        }
        AlignedBox(Alignment.TopEnd) {
            CloseButton(onDismiss)
        }
        AlignedBox(Alignment.Center) {
            CenterColumn(Modifier.padding(64.dp)) {
                SelectionContainer {
                    Text(image.description)
                }
                GeneralSpacer()
                Button(onInstall) {
                    Text(stringResource(Res.string.install_os))
                }
            }
        }
        AlignedBox(Alignment.BottomStart) {
            SelectionContainer {
                Text(
                    text = sourcesText,
                    color = Color.Gray,
                )
            }
        }
    }
}
package io.github.bommbomm34.intervirt.gui.imagepicker

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.install_os
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.Overlay
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun ImageInfo(
    image: Image,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
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
    }
}
package io.github.bommbomm34.intervirt.gui.imagepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.install_new_os
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun ImageInfo(
    image: Image,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
){
    AlignedBox(Alignment.TopStart){
        ImageIcon(image)
    }
    AlignedBox(Alignment.TopCenter){
        Text(
            text = image.name,
            fontSize = 24.sp
        )
    }
    AlignedBox(Alignment.TopEnd){
        CloseButton(onDismiss)
    }
    AlignedBox(Alignment.Center){
        Column {
            Text(image.description)
            GeneralSpacer()
            Button(onInstall){
                Text(stringResource(Res.string.install_new_os))
            }
        }
    }
}
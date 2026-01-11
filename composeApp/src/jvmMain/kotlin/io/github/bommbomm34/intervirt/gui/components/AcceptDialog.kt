package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.no
import intervirt.composeapp.generated.resources.yes
import io.github.bommbomm34.intervirt.closeDialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun AcceptDialog(
    message: String,
    onCancel: () -> Unit = {},
    onAccept: () -> Unit,
){
    CenterColumn {
        Text(message)
        GeneralSpacer()
        Row {
            Button(onClick = {
                closeDialog()
                onAccept()
            }){
                Text(stringResource(Res.string.yes))
            }
            GeneralSpacer()
            Button(onClick = {
                closeDialog()
                onCancel()
            }){
                Text(stringResource(Res.string.no))
            }
        }
    }
}
package io.github.bommbomm34.intervirt.gui.components.filepicker

import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.filename
import intervirt.composeapp.generated.resources.save
import io.github.bommbomm34.intervirt.gui.components.CenterRow
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileSaveView(
    filename: String,
    onSave: (String) -> Unit
){
    var currentFilename by remember { mutableStateOf(filename) }
    GeneralSpacer()
    CenterRow {
        OutlinedTextField(
            value = currentFilename,
            onValueChange = { currentFilename = it },
            label = { Text(stringResource(Res.string.filename)) }
        )
        GeneralSpacer()
        Button(
            onClick = { onSave(currentFilename) }
        ){
            Text(stringResource(Res.string.save))
        }
    }
}
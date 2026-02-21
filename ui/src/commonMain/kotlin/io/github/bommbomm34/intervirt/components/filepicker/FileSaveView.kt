package io.github.bommbomm34.intervirt.components.filepicker

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.filename
import intervirt.ui.generated.resources.save
import io.github.bommbomm34.intervirt.components.CenterRow
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileSaveView(
    filename: String,
    onSave: (String) -> Unit,
) {
    var currentFilename by remember { mutableStateOf(filename) }
    GeneralSpacer()
    CenterRow {
        OutlinedTextField(
            value = currentFilename,
            onValueChange = { currentFilename = it },
            label = { Text(stringResource(Res.string.filename)) },
        )
        GeneralSpacer()
        Button(
            onClick = { onSave(currentFilename) },
        ) {
            Text(stringResource(Res.string.save))
        }
    }
}
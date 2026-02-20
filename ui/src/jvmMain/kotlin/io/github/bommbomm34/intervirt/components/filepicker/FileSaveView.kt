package io.github.bommbomm34.intervirt.components.filepicker

import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.filename
import intervirt.ui.generated.resources.save
import io.github.bommbomm34.intervirt.gui.components.CenterRow
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileSaveView(
    filename: String,
    onSave: (String) -> Unit,
) {
    var currentFilename by remember { mutableStateOf(filename) }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
    _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterRow {
        OutlinedTextField(
            value = currentFilename,
            onValueChange = { currentFilename = it },
            label = { Text(stringResource(Res.string.filename)) },
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        Button(
            onClick = { onSave(currentFilename) },
        ) {
            Text(stringResource(Res.string.save))
        }
    }
}
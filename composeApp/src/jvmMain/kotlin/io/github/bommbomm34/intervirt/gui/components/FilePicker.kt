package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.pick_directory
import intervirt.composeapp.generated.resources.pick_file
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilePicker(
    label: String? = null,
    directory: Boolean = false,
    defaultPath: String = "",
    onResult: (PlatformFile) -> Unit
) {
    var path by remember { mutableStateOf(defaultPath) }
    val scope: (PlatformFile?) -> Unit = { file ->
        file?.let {
            path = it.path
            onResult(it)
        }
    }
    val launcher = if (directory) rememberDirectoryPickerLauncher(onResult = scope) else
        rememberFilePickerLauncher(onResult = scope)
    Column {
        CenterRow {
            Button(onClick = { launcher.launch() }) {
                Text(stringResource(if (directory) Res.string.pick_directory else Res.string.pick_file))
            }
            GeneralSpacer(8.dp)
            Text(path)
        }
        label?.let { Text(label, fontSize = 12.sp, color = Color.Gray) }
    }
}
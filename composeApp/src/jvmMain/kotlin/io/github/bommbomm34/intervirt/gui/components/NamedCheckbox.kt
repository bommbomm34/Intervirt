package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun NamedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    name: String
){
    Row (verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        GeneralSpacer(2.dp)
        Text(name)
    }
}
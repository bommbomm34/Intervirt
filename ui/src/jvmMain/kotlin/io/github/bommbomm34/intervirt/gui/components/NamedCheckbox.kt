package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.core.data.AppEnv
import org.koin.compose.koinInject

@Composable
fun NamedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    name: String,
    tooltip: String? = null
){
    val appEnv = koinInject<AppEnv>()
    Row (verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        GeneralSpacer(2.dp)
        Column {
            Text(name)
            tooltip?.let { Text(tooltip, fontSize = appEnv.tooltipFontSize.sp, color = Color.Gray) }
        }
    }
}
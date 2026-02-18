package io.github.bommbomm34.intervirt.gui.components.textfields

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun IntegerTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) = OutlinedTextField(
    value = value.toString(),
    onValueChange = { newValue -> newValue.toIntOrNull()?.let { onValueChange(it) } },
    label = { Text(label) },
    modifier = modifier,
)
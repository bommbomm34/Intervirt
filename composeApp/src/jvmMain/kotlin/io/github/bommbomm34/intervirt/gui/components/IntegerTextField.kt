package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun IntegerTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String
) = OutlinedTextField(
    value = value.toString(),
    onValueChange = { newValue -> newValue.toIntOrNull()?.let { onValueChange(it) } },
    label = { Text(label) }
)
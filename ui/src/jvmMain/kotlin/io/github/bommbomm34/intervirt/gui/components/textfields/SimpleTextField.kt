package io.github.bommbomm34.intervirt.gui.components.textfields

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun SimpleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    isError: Boolean = false,
) = OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = label?.let { { Text(it) } },
    isError = isError
)
package io.github.bommbomm34.intervirt.components.textfields

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
    isError = isError,
)

@Composable
fun ReadOnlyTextField(
    value: String,
    label: String? = null,
) = OutlinedTextField(
    value = value,
    onValueChange = {},
    label = label?.let { { Text(it) } },
)
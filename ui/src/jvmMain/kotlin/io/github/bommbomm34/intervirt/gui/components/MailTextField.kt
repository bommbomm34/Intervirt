package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

private val ADDRESS_VALIDATION_REGEX = Regex(".+:\\d+")

@Composable
fun AddressTextField(
    value: String,
    valid: Boolean,
    onValueChange: (String, Boolean) -> Unit,
    label: String,
    errorLabel: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it, it.validate()) },
        label = {
            if (valid) Text(label) else
                Text(
                    color = MaterialTheme.colors.error,
                    text = errorLabel
                )
        }
    )
}

private fun String.validate() = ADDRESS_VALIDATION_REGEX.matches(this)
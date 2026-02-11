package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.password
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit
){
    // TODO: Add visibility toggle field
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(Res.string.password)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = VisualTransformation.None
    )
}
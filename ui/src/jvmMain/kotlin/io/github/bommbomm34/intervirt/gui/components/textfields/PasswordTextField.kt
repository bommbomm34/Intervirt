package io.github.bommbomm34.intervirt.gui.components.textfields

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.SecureTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.password
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasswordTextField(
    state: TextFieldState
) {
    SecureTextField(
        state = state,
        label = { Text(stringResource(Res.string.password)) },
    )
}
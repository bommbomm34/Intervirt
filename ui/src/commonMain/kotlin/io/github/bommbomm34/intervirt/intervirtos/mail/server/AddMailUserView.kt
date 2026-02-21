package io.github.bommbomm34.intervirt.intervirtos.mail.server

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.add_user
import intervirt.ui.generated.resources.email_address
import intervirt.ui.generated.resources.username
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.components.textfields.PasswordTextField
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.core.data.MailUser
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddMailUserView(
    mailServer: MailServerManager,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    val password = rememberTextFieldState()
    AlignedBox(Alignment.TopStart) {
        CloseButton(onClose)
    }
    CenterColumn {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(Res.string.username)) },
        )
        GeneralSpacer()
        OutlinedTextField(
            value = emailAddress,
            onValueChange = { emailAddress = it },
            label = { Text(stringResource(Res.string.email_address)) },
        )
        GeneralSpacer()
        PasswordTextField(password)
        GeneralSpacer()
        Button(
            onClick = {
                scope.launch {
                    mailServer.addMailUser(MailUser(username, emailAddress), password.text.toString()).getOrThrow()
                    username = ""
                    emailAddress = ""
                    password.clearText()
                    onClose()
                }
            },
        ) {
            Text(stringResource(Res.string.add_user))
        }
    }
}
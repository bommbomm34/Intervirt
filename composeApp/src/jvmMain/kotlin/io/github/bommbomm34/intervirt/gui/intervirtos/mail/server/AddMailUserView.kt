package io.github.bommbomm34.intervirt.gui.intervirtos.mail.server

import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.add_user
import intervirt.composeapp.generated.resources.email_address
import intervirt.composeapp.generated.resources.username
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.gui.components.CenterRow
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.PasswordTextField
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddMailUserView(
    mailServer: MailServerManager
){
    // TODO: Save the password more secure
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    CenterRow {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(Res.string.username)) }
        )
        GeneralSpacer()
        OutlinedTextField(
            value = emailAddress,
            onValueChange = { emailAddress = it },
            label = { Text(stringResource(Res.string.email_address)) }
        )
        GeneralSpacer()
        PasswordTextField(
            value = password,
            onValueChange = { password = it }
        )
        GeneralSpacer()
        Button(
            onClick = {
                scope.launch {
                    mailServer.addMailUser(MailUser(username, emailAddress), password).getOrThrow()
                    username = ""
                    emailAddress = ""
                    password = ""
                }
            }
        ){
            Text(stringResource(Res.string.add_user))
        }
    }
}
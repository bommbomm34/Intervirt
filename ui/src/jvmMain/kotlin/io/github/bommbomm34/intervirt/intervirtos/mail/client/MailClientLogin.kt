package io.github.bommbomm34.intervirt.intervirtos.mail.client

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionDetails
import io.github.bommbomm34.intervirt.core.parseAddress
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.CenterRow
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.components.textfields.AddressTextField
import io.github.bommbomm34.intervirt.gui.components.textfields.PasswordTextField
import org.jetbrains.compose.resources.stringResource

@Composable
fun MailClientLogin(
    credentials: MailConnectionDetails,
    onLogin: (MailConnectionDetails, saveCredentials: Boolean) -> Unit,
) {
    var smtpAddress by remember { mutableStateOf(credentials.smtpAddress.toString()) }
    var imapAddress by remember { mutableStateOf(credentials.imapAddress.toString()) }
    var smtpSafety by remember { mutableStateOf(credentials.smtpSafety) }
    var imapSafety by remember { mutableStateOf(credentials.imapSafety) }
    var isSmtpAddressValid by remember { mutableStateOf(true) }
    var isImapAddressValid by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf(credentials.username) }
    var password = rememberTextFieldState(credentials.password)
    var saveDetails by remember { mutableStateOf(false) }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterColumn {
        // SMTP Address
        _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterRow {
            AddressTextField(
                value = smtpAddress,
                valid = isSmtpAddressValid,
                onValueChange = { it, valid ->
                    smtpAddress = it
                    isSmtpAddressValid = valid
                },
                label = stringResource(Res.string.server_address, "SMTP"),
                errorLabel = stringResource(Res.string.invalid_server_address, "SMTP"),
            )
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            MailSafetyChooser(
                protocol = "SMTP",
                safety = smtpSafety,
                onSafetyChange = { smtpSafety = it },
            )
        }
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        // IMAP Address
        _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterRow {
            AddressTextField(
                value = imapAddress,
                valid = isImapAddressValid,
                onValueChange = { it, valid ->
                    imapAddress = it
                    isImapAddressValid = valid
                },
                label = stringResource(Res.string.server_address, "IMAP"),
                errorLabel = stringResource(Res.string.invalid_server_address, "IMAP"),
            )
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            MailSafetyChooser(
                protocol = "IMAP",
                safety = imapSafety,
                onSafetyChange = { imapSafety = it },
            )
        }
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        // Mail username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(Res.string.username)) },
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        // Mail password
        _root_ide_package_.io.github.bommbomm34.intervirt.components.textfields.PasswordTextField(password)
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        // Remember checkbox
        _root_ide_package_.io.github.bommbomm34.intervirt.components.NamedCheckbox(
            checked = saveDetails,
            onCheckedChange = { saveDetails = it },
            name = stringResource(Res.string.save_login_details),
            tooltip = stringResource(Res.string.save_login_details_warning),
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        Button(
            onClick = {
                onLogin(
                    MailConnectionDetails(
                        smtpAddress = smtpAddress.parseAddress(),
                        smtpSafety = smtpSafety,
                        imapAddress = imapAddress.parseAddress(),
                        imapSafety = imapSafety,
                        username = username,
                        password = password.text.toString(),
                    ),
                    saveDetails,
                )
            },
            enabled = isSmtpAddressValid && smtpAddress.isNotBlank() &&
                    isImapAddressValid && imapAddress.isNotBlank(),
        ) {
            Text(stringResource(Res.string.login))
        }
    }
}
package io.github.bommbomm34.intervirt.gui.intervirtos.mail.client

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
import io.github.bommbomm34.intervirt.gui.components.textfields.AddressTextField
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
    var password by remember { mutableStateOf(credentials.password) }
    var saveDetails by remember { mutableStateOf(false) }
    CenterColumn {
        // SMTP Address
        CenterRow {
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
            GeneralSpacer()
            MailSafetyChooser(
                protocol = "SMTP",
                safety = smtpSafety,
                onSafetyChange = { smtpSafety = it },
            )
        }
        GeneralSpacer()
        // IMAP Address
        CenterRow {
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
            GeneralSpacer()
            MailSafetyChooser(
                protocol = "IMAP",
                safety = imapSafety,
                onSafetyChange = { imapSafety = it },
            )
        }
        GeneralSpacer()
        // Mail username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(Res.string.username)) },
        )
        GeneralSpacer()
        // Mail password
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
        )
        GeneralSpacer()
        // Remember checkbox
        NamedCheckbox(
            checked = saveDetails,
            onCheckedChange = { saveDetails = it },
            name = stringResource(Res.string.save_login_details),
            tooltip = stringResource(Res.string.save_login_details_warning),
        )
        GeneralSpacer()
        Button(
            onClick = {
                onLogin(
                    MailConnectionDetails(
                        smtpAddress = smtpAddress.parseAddress(),
                        smtpSafety = smtpSafety,
                        imapAddress = imapAddress.parseAddress(),
                        imapSafety = imapSafety,
                        username = username,
                        password = password,
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
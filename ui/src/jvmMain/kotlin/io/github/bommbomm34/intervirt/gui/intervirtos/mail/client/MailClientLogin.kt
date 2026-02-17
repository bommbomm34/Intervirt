package io.github.bommbomm34.intervirt.gui.intervirtos.mail.client

import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.api.Preferences
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionDetails
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionSafety
import io.github.bommbomm34.intervirt.core.parseAddress
import io.github.bommbomm34.intervirt.gui.components.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MailClientLogin(
    onLogin: (MailConnectionDetails) -> Unit
) {
    val appEnv = koinInject<AppEnv>()
    val preferences = koinInject<Preferences>()
    var smtpAddress by remember { mutableStateOf(appEnv.smtpServerAddress) }
    var imapAddress by remember { mutableStateOf(appEnv.imapServerAddress) }
    var smtpSafety by remember { mutableStateOf(MailConnectionSafety.SECURE) }
    var imapSafety by remember { mutableStateOf(MailConnectionSafety.SECURE) }
    var isSmtpAddressValid by remember { mutableStateOf(true) }
    var isImapAddressValid by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf(appEnv.mailUsername) }
    var password by remember { mutableStateOf(appEnv.mailPassword) }
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
                errorLabel = stringResource(Res.string.invalid_server_address, "SMTP")
            )
            GeneralSpacer()
            MailSafetyChooser(
                protocol = "SMTP",
                safety = smtpSafety,
                onSafetyChange = { smtpSafety = it }
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
                errorLabel = stringResource(Res.string.invalid_server_address, "IMAP")
            )
            GeneralSpacer()
            MailSafetyChooser(
                protocol = "IMAP",
                safety = imapSafety,
                onSafetyChange = { imapSafety = it }
            )
        }
        GeneralSpacer()
        // Mail username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(Res.string.username)) }
        )
        GeneralSpacer()
        // Mail password
        PasswordTextField(
            value = password,
            onValueChange = { password = it }
        )
        GeneralSpacer()
        // Remember checkbox
        NamedCheckbox(
            checked = saveDetails,
            onCheckedChange = { saveDetails = it },
            name = stringResource(Res.string.save_login_details),
            tooltip = stringResource(Res.string.save_login_details_warning)
        )
        GeneralSpacer()
        Button(
            onClick = {
                if (saveDetails) {
                    appEnv.saveCredentials(smtpAddress, imapAddress, username, password)
                } else preferences.clearCredentials()
                onLogin(
                    MailConnectionDetails(
                        smtpAddress = smtpAddress.parseAddress(),
                        smtpSafety = smtpSafety,
                        imapAddress = imapAddress.parseAddress(),
                        imapSafety = imapSafety,
                        username = username,
                        password = password
                    )
                )
            },
            enabled = isSmtpAddressValid && smtpAddress.isNotBlank() &&
                    isImapAddressValid && imapAddress.isNotBlank()
        ) {
            Text(stringResource(Res.string.login))
        }
    }
}

private fun AppEnv.saveCredentials(
    smtpAddress: String,
    imapAddress: String,
    username: String,
    password: String
) {
    smtpServerAddress = smtpAddress
    imapServerAddress = imapAddress
    mailUsername = username
    mailPassword = password
}

private fun Preferences.clearCredentials() {
    removeString("SMTP_SERVER_ADDRESS")
    removeString("IMAP_SERVER_ADDRESS")
    removeString("MAIL_USERNAME")
    removeString("MAIL_PASSWORD")
}


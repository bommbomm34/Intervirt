package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Refresh
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.refresh
import intervirt.ui.generated.resources.sure_to_delete_mail
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailClientManager
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionDetails
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.*
import io.github.bommbomm34.intervirt.gui.components.buttons.SendButton
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.client.MailClientLogin
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.client.MailEditor
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.client.MailListView
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.client.MailView
import io.github.bommbomm34.intervirt.rememberLogger
import io.github.bommbomm34.intervirt.rememberManager
import io.github.bommbomm34.intervirt.rememberProxyManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MailClient(
    osClient: IntervirtOSClient,
) {
    val logger = rememberLogger("MailClient")
    val appState = koinInject<AppState>()
    val appEnv = koinInject<AppEnv>()
    val deviceManager = koinInject<DeviceManager>()
    val client = osClient.rememberManager(::MailClientManager)
    val proxyClient = rememberProxyManager(appEnv, deviceManager, osClient)
    val mails = remember { mutableStateListOf<Mail>() }
    var proxyUrl: Address? by remember { mutableStateOf(null) }
    var initialized by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // Get proxy
    LaunchedEffect(Unit) {
        appState.runDialogCatching {
            proxyUrl = proxyClient.getProxyUrl().getOrThrow()
        }
    }
    suspend fun loadMails() {
        mails.clear()
        mails.addAll(client.getMails().getOrThrow())
    }

    fun openMailEditor(mail: Mail? = null) {
        appState.openDialog {
            MailEditor(
                sender = client.mailUser!!,
                mail = mail,
                onCancel = appState::closeDialog,
            ) {
                appState.closeDialog()
                scope.launch {
                    appState.runDialogCatching {
                        client.sendMail(it).getOrThrow()
                    }
                }
            }
        }
    }
    proxyUrl?.let { proxy ->
        if (initialized) {
            // Send button
            AlignedBox(Alignment.BottomEnd) {
                SendButton { openMailEditor() }
            }
            CenterColumn {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    // Refresh button
                    IconButton(
                        onClick = {
                            scope.launch {
                                appState.runDialogCatching {
                                    loadMails()
                                }
                            }
                        },
                    ) {
                        GeneralIcon(
                            imageVector = TablerIcons.Refresh,
                            contentDescription = stringResource(Res.string.refresh),
                        )
                    }
                }
                GeneralSpacer(2.dp)
                MailListView(mails) {
                    appState.openDialog {
                        MailView(
                            mail = it,
                            onDelete = {
                                appState.closeDialog()
                                appState.openDialog {
                                    AcceptDialog(
                                        message = stringResource(Res.string.sure_to_delete_mail),
                                    ) {
                                        scope.launch {
                                            appState.runDialogCatching {
                                                client.deleteMail(it).getOrThrow()
                                                mails.remove(it)
                                            }
                                        }
                                    }
                                }
                            },
                            onReply = {
                                appState.closeDialog()
                                scope.launch {
                                    appState.runDialogCatching {
                                        val mail = client.getReplyMail(it).getOrThrow()
                                        openMailEditor(mail)
                                    }
                                }
                            },
                        ) { appState.closeDialog() }
                    }
                }
            }
        } else {
            fun login(details: MailConnectionDetails, saveCredentials: Boolean) {
                scope.launch {
                    appState.runDialogCatching {
                        client.init(
                            mailConnectionDetails = details,
                            proxy = proxy,
                        ).getOrThrow()
                        initialized = true
                        loadMails()
                        if (saveCredentials) client.saveCredentials(details).getOrThrow() else
                            client.clearCredentials()
                    }
                }
            }

            val credentials = client.loadCredentials()
            if (credentials.smtpAddress != Address.EXAMPLE
                && credentials.imapAddress != Address.EXAMPLE
                && credentials.username.isNotEmpty()
                && credentials.password.isNotEmpty()
            ) {
                // Implicit login
                login(credentials, true)
            } else {
                appState.openDialog {
                    MailClientLogin(credentials) { details, saveCredentials ->
                        appState.closeDialog()
                        login(details, saveCredentials)
                    }
                }
            }
        }
    }
}
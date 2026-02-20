package io.github.bommbomm34.intervirt.intervirtos

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
import io.github.bommbomm34.intervirt.gui.components.dialogs.AcceptDialog
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
            _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.mail.client.MailEditor(
                sender = client.mailUser!!,
                mail = mail,
                onCancel = ::close,
            ) {
                close()
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
            _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.BottomEnd) {
                _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.SendButton { openMailEditor() }
            }
            _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterColumn {
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
                        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralIcon(
                            imageVector = TablerIcons.Refresh,
                            contentDescription = stringResource(Res.string.refresh),
                        )
                    }
                }
                _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer(2.dp)
                _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.mail.client.MailListView(mails) {
                    appState.openDialog {
                        _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.mail.client.MailView(
                            mail = it,
                            onDelete = {
                                close()
                                appState.openDialog {
                                    _root_ide_package_.io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog(
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
                                close()
                                scope.launch {
                                    appState.runDialogCatching {
                                        val mail = client.getReplyMail(it).getOrThrow()
                                        openMailEditor(mail)
                                    }
                                }
                            },
                            onClose = ::close
                        )
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
                    _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.mail.client.MailClientLogin(
                        credentials
                    ) { details, saveCredentials ->
                        close()
                        login(details, saveCredentials)
                    }
                }
            }
        }
    }
}
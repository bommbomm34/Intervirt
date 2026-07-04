/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos.model

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.Raise
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.sure_to_delete_mail
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.components.dialogs.openAcceptDialog
import io.github.bommbomm34.intervirt.core.api.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.getValue
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailClientManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.ProxyManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionDetails
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.data.runDialogCatching
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailClientLogin
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailEditor
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailView
import io.github.bommbomm34.intervirt.secret.SecretService
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MailClientViewModel(
    private val appState: AppState,
    envHolder: AppEnvHolder,
    deviceManager: DeviceManager,
    secretService: SecretService,
    @InjectedParam val osClient: IntervirtOSClient,
) : ViewModel() {
    val proxyClient = ProxyManager(envHolder, deviceManager, osClient)
    val client = MailClientManager(osClient, envHolder, secretService)

    val mails = mutableStateListOf<Mail>()
    var proxyUrl: Address? by mutableStateOf(null)
    var initialized by mutableStateOf(false)

    init {
        viewModelScope.launchDialogCatching(appState) {
            proxyUrl = proxyClient.getProxyUrl()
        }
    }

    fun loadMails() {
        viewModelScope.launchDialogCatching(appState) {
            loadMailsInternal()
        }
    }

    fun clickMail(mail: Mail) {
        appState.openDialog(height = 600.dp) {
            val scope = rememberCoroutineScope()
            MailView(
                mail = mail,
                onDelete = {
                    close()
                    appState.openAcceptDialog(Res.string.sure_to_delete_mail) {
                        close()
                        scope.launch {
                            appState.runDialogCatching {
                                client.deleteMail(mail)
                                mails.remove(mail)
                            }
                        }
                    }
                },
                onReply = {
                    close()
                    scope.launch {
                        appState.runDialogCatching {
                            val mail = client.getReplyMail(mail)
                            openMailEditor(mail)
                        }
                    }
                },
                onClose = ::close,
            )
        }
    }

    fun login(
        details: MailConnectionDetails,
        saveCredentials: Boolean,
        proxy: Address,
    ) {
        viewModelScope.launchDialogCatching(appState) {
            client.init(
                mailConnectionDetails = details,
                proxy = proxy,
            )
            initialized = true
            loadMails()
            if (saveCredentials) client.saveCredentials(details) else
                client.clearCredentials()
        }
    }

    fun newLogin(creds: MailConnectionDetails, proxy: Address) {
        appState.openDialog(height = 600.dp) {
            MailClientLogin(
                credentials = creds,
            ) { details, saveCredentials ->
                close()
                login(details, saveCredentials, proxy)
            }
        }
    }

    context(_: Raise<Failure>)
    private suspend fun loadMailsInternal() {
        mails.clear()
        mails.addAll(client.getMails())
    }

    fun openMailEditor(mail: Mail? = null) {
        appState.openDialog {
            val scope = rememberCoroutineScope()
            MailEditor(
                sender = client.mailUser!!,
                mail = mail,
                onCancel = ::close,
            ) {
                close()
                scope.launch {
                    appState.runDialogCatching {
                        client.sendMail(it)
                    }
                }
            }
        }
    }
}

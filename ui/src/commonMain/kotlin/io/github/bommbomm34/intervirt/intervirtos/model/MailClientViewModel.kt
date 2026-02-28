package io.github.bommbomm34.intervirt.intervirtos.model

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.sure_to_delete_mail
import io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailClientManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.ProxyManager
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionDetails
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailClientLogin
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailEditor
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailView
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MailClientViewModel(
    private val appState: AppState,
    private val appEnv: AppEnv,
    @InjectedParam val client: MailClientManager,
    @InjectedParam val proxyClient: ProxyManager,
) : ViewModel() {
    val mails = mutableStateListOf<Mail>()
    var proxyUrl: Address? by mutableStateOf(null)
    var initialized by mutableStateOf(false)

    init {
        viewModelScope.launchDialogCatching(appState) {
            proxyUrl = proxyClient.getProxyUrl().getOrThrow()
        }
    }

    fun loadMails() {
        viewModelScope.launchDialogCatching(appState) {
            loadMailsInternal()
        }
    }

    fun clickMail(mail: Mail) {
        appState.openDialog {
            val scope = rememberCoroutineScope()
            MailView(
                mail = mail,
                onDelete = {
                    close()
                    appState.openDialog {
                        AcceptDialog(
                            message = stringResource(Res.string.sure_to_delete_mail),
                            onCancel = ::close,
                        ) {
                            close()
                            scope.launch {
                                appState.runDialogCatching {
                                    client.deleteMail(mail).getOrThrow()
                                    mails.remove(mail)
                                }
                            }
                        }
                    }
                },
                onReply = {
                    close()
                    scope.launch {
                        appState.runDialogCatching {
                            val mail = client.getReplyMail(mail).getOrThrow()
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
            ).getOrThrow()
            initialized = true
            loadMails()
            if (saveCredentials) client.saveCredentials(details).getOrThrow() else
                client.clearCredentials()
        }
    }

    fun newLogin(creds: MailConnectionDetails, proxy: Address) {
        appState.openDialog {
            MailClientLogin(
                credentials = creds,
            ) { details, saveCredentials ->
                close()
                login(details, saveCredentials, proxy)
            }
        }
    }

    private suspend fun loadMailsInternal() {
        mails.clear()
        mails.addAll(client.getMails().getOrThrow())
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
                        client.sendMail(it).getOrThrow()
                    }
                }
            }
        }
    }
}
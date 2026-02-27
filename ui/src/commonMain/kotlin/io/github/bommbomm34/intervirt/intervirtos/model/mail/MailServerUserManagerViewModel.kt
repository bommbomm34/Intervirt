package io.github.bommbomm34.intervirt.intervirtos.model.mail

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.sure_to_delete_user
import io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.intervirtos.mail.server.AddMailUserView
import org.jetbrains.compose.resources.stringResource
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.collections.remove

@KoinViewModel
class MailServerUserManagerViewModel(
    val appState: AppState,
    @InjectedParam val mailServer: MailServerManager,
) : ViewModel() {
    val users = mutableStateListOf<MailUser>()

    init {
        retrieveUsers()
    }

    fun retrieveUsers() {
        viewModelScope.launchDialogCatching(appState) {
            val newUsers = mailServer.listMailUsers().getOrThrow()
            users.clear()
            users.addAll(newUsers)
        }
    }

    fun removeUser(user: MailUser){
        appState.openDialog {
            val scope = rememberCoroutineScope()
            AcceptDialog(
                message = stringResource(Res.string.sure_to_delete_user),
                onCancel = ::close,
            ) {
                close()
                scope.launchDialogCatching(appState) {
                    mailServer.removeMailUser(user).getOrThrow()
                    users.remove(user)
                }
            }
        }
    }

    fun addUser(){
        appState.openDialog {
            val scope = rememberCoroutineScope()
            AddMailUserView(mailServer) {
                close()
                scope.launchDialogCatching(appState) {
                    retrieveUsers()
                }
            }
        }
    }
}
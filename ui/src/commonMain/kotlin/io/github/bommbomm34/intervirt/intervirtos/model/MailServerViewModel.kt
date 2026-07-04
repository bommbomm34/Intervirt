/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.sure_to_delete_user
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.components.dialogs.openAcceptDialog
import io.github.bommbomm34.intervirt.core.api.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.intervirtos.mail.server.AddMailUserView
import io.github.bommbomm34.intervirt.util.ext.initDocker
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MailServerViewModel(
    @InjectedParam val osClient: IntervirtOSClient,
    private val appState: AppState,
    envHolder: AppEnvHolder,
) : ViewModel() {
    val mailServer = MailServerManager(envHolder, osClient)
    var initialized by mutableStateOf(false)
    val users = mutableStateListOf<MailUser>()

    init {
        viewModelScope.initDocker(
            appState = appState,
            manager = mailServer,
        ) { initialized = true }
    }

    fun retrieveUsers() {
        viewModelScope.launchDialogCatching(appState) {
            val newUsers = mailServer.listMailUsers()
            users.clear()
            users.addAll(newUsers)
        }
    }

    fun removeUser(user: MailUser) {
        appState.openAcceptDialog(Res.string.sure_to_delete_user) {
            close()
            viewModelScope.launchDialogCatching(appState) {
                mailServer.removeMailUser(user)
                users.remove(user)
            }
        }
    }

    fun addUser() {
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

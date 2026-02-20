package io.github.bommbomm34.intervirt.intervirtos.mail.server

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.email_address
import intervirt.ui.generated.resources.sure_to_delete_user
import intervirt.ui.generated.resources.username
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.dialogs.AcceptDialog
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.AddButton
import io.github.bommbomm34.intervirt.gui.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.gui.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.gui.components.tables.SimpleTable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val headers = listOf(
    Res.string.username,
    Res.string.email_address,
)

@Composable
fun MailServerUserManager(
    mailServer: MailServerManager,
) {
    val appState = koinInject<AppState>()
    val users = remember { mutableStateListOf<MailUser>() }
    val scope = rememberCoroutineScope()
    suspend fun retrieveUsers(){
        val newUsers = mailServer.listMailUsers().getOrThrow()
        users.clear()
        users.addAll(newUsers)
    }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect {
        retrieveUsers()
    }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
    _root_ide_package_.io.github.bommbomm34.intervirt.components.tables.SimpleTable(
        headers = headers.map { stringResource(it) } + "",
        content = users.map { listOf(it.username, it.address) },
        customElements = users.map {
            {
                _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.RemoveButton {
                    appState.openDialog {
                        _root_ide_package_.io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog(
                            message = stringResource(Res.string.sure_to_delete_user),
                            onCancel = ::close
                        ) {
                            scope.launchDialogCatching(appState) {
                                mailServer.removeMailUser(it).getOrThrow()
                                users.remove(it)
                            }
                        }
                    }
                }
            }
        },
    )
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.BottomEnd) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.AddButton {
            appState.openDialog {
                AddMailUserView(mailServer) {
                    close()
                    scope.launchDialogCatching(appState) {
                        retrieveUsers()
                    }
                }
            }
        }
    }
}
package io.github.bommbomm34.intervirt.gui.intervirtos.mail.server

import androidx.compose.runtime.*
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.email_address
import intervirt.composeapp.generated.resources.username
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.SimpleTable
import io.github.bommbomm34.intervirt.gui.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.runSuspendingCatching
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val headers = listOf(
    Res.string.username,
    Res.string.email_address
)

@Composable
fun MailServerUserManager(mailServer: MailServerManager){
    val users = remember { mutableStateListOf<MailUser>() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit){
        runSuspendingCatching {
            val newUsers = mailServer.listMailUsers().getOrThrow()
            users.clear()
            users.addAll(newUsers)
        }
    }
    AddMailUserView(mailServer)
    GeneralSpacer()
    SimpleTable(
        headers = headers.map { stringResource(it) },
        content = users.map { listOf(it.username, it.address) },
        customElements = users.map {
            {
                RemoveButton {
                    scope.launch {
                        runSuspendingCatching {
                            mailServer.removeMailUser(it.username).getOrThrow()
                        }
                    }
                }
            }
        }
    )
}
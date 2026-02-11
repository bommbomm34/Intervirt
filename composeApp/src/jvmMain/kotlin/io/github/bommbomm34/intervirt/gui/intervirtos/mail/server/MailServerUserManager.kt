package io.github.bommbomm34.intervirt.gui.intervirtos.mail.server

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.email_address
import intervirt.composeapp.generated.resources.username
import io.github.bommbomm34.intervirt.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.data.MailUser
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
fun MailServerUserManager(osClient: IntervirtOSClient){
    val users = remember { mutableStateListOf<MailUser>() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit){
        runSuspendingCatching {
            val newUsers = osClient.listMailUsers().getOrThrow()
            users.clear()
            users.addAll(newUsers)
        }
    }
    AddMailUserView(osClient)
    GeneralSpacer()
    SimpleTable(
        headers = headers.map { stringResource(it) },
        content = users.map { listOf(it.username, it.address) },
        customElements = users.map {
            {
                RemoveButton {
                    scope.launch {
                        runSuspendingCatching {
                            osClient.removeMailUser(it.username).getOrThrow()
                        }
                    }
                }
            }
        }
    )
}
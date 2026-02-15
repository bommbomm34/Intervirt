package io.github.bommbomm34.intervirt.gui.intervirtos.mail.client

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.receiver
import intervirt.ui.generated.resources.subject
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailClientManager
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.SimpleTable
import io.github.bommbomm34.intervirt.gui.components.buttons.RemoveButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MailListView(
    client: MailClientManager,
    mails: List<Mail>,
    onClick: (Mail) -> Unit
){
    val appState = koinInject<AppState>()
    val scope = rememberCoroutineScope()
    // TODO: Make mails clickable
    SimpleTable(
        headers = listOf(stringResource(Res.string.receiver), stringResource(Res.string.subject)),
        content = mails.map { listOf(it.receiver, it.subject) },
        customElements = mails.map {
            {
                RemoveButton {
                    scope.launch {
                        appState.runDialogCatching {
                            client.deleteMail(it).getOrThrow()
                        }
                    }
                }
            }
        }
    )
}
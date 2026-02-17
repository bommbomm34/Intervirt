package io.github.bommbomm34.intervirt.gui.intervirtos.mail.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.sender
import intervirt.ui.generated.resources.subject
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailClientManager
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.tables.ClickableTable
import io.github.bommbomm34.intervirt.gui.components.tables.VisibleText
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MailListView(
    mails: List<Mail>,
    onClick: (Mail) -> Unit
) {
    val headers = listOf(stringResource(Res.string.sender), stringResource(Res.string.subject))
    ClickableTable(
        headers = headers,
        data = mails.map {
            listOf(
                { VisibleText(it.sender) },
                { VisibleText(it.receiver) },
            )
        }
    ){ onClick(mails[it]) }
}
package io.github.bommbomm34.intervirt.gui.intervirtos.mail.client

import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.sender
import intervirt.ui.generated.resources.subject
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.gui.components.tables.ClickableTable
import io.github.bommbomm34.intervirt.gui.components.tables.VisibleText
import org.jetbrains.compose.resources.stringResource

@Composable
fun MailListView(
    mails: List<Mail>,
    onClick: (Mail) -> Unit,
) {
    val headers = listOf(stringResource(Res.string.subject), stringResource(Res.string.sender))
    ClickableTable(
        headers = headers,
        data = mails.map {
            listOf(
                { VisibleText(it.subject) },
                { VisibleText(it.sender) },
            )
        },
    ) { onClick(mails[it]) }
}
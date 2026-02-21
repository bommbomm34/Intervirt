package io.github.bommbomm34.intervirt.intervirtos.mail.client

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.sender
import intervirt.ui.generated.resources.subject
import io.github.bommbomm34.intervirt.components.tables.ClickableTable
import io.github.bommbomm34.intervirt.core.data.Mail
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
                { Text(it.subject) },
                { Text(it.sender.toString()) },
            )
        },
    ) { onClick(mails[it]) }
}
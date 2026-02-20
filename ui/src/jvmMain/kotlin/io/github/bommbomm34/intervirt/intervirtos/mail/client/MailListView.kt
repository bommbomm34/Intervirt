package io.github.bommbomm34.intervirt.intervirtos.mail.client

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
    _root_ide_package_.io.github.bommbomm34.intervirt.components.tables.ClickableTable(
        headers = headers,
        data = mails.map {
            listOf(
                { _root_ide_package_.io.github.bommbomm34.intervirt.components.tables.VisibleText(it.subject) },
                { _root_ide_package_.io.github.bommbomm34.intervirt.components.tables.VisibleText(it.sender) },
            )
        },
    ) { onClick(mails[it]) }
}
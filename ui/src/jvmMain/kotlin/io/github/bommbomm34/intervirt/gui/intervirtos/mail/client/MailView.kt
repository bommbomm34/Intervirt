package io.github.bommbomm34.intervirt.gui.intervirtos.mail.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Send
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.from
import intervirt.ui.generated.resources.reply
import intervirt.ui.generated.resources.to
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.RemoveButton
import org.jetbrains.compose.resources.stringResource

private val FROM_TO_COLOR = Color.Gray

@Composable
fun MailView(
    mail: Mail,
    onDelete: () -> Unit,
    onReply: () -> Unit
) {
    CenterColumn {
        // Subject, From, To and Content
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(mail.subject)
            GeneralSpacer()
            Text(
                text = stringResource(Res.string.from, mail.sender),
                color = FROM_TO_COLOR
            )
            GeneralSpacer(2.dp)
            Text(
                text = stringResource(Res.string.to, mail.receiver),
                color = FROM_TO_COLOR
            )
            GeneralSpacer()
            SelectionContainer(
                modifier = Modifier
                    .fillMaxSize(0.6f)
                    .background(MaterialTheme.colors.background.copy(alpha = 0.5f))
            ) {
                Text(mail.content)
            }
        }
        // Delete, Reply
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Delete
            RemoveButton(onDelete)
            GeneralSpacer()
            // Reply
            IconButton(onReply) {
                GeneralIcon(
                    imageVector = TablerIcons.Send,
                    contentDescription = stringResource(Res.string.reply)
                )
            }
        }
    }
}
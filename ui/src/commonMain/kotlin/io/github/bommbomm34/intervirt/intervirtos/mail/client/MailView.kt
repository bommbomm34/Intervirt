package io.github.bommbomm34.intervirt.intervirtos.mail.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.Send
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.from
import intervirt.ui.generated.resources.reply
import intervirt.ui.generated.resources.to
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Mail
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val FROM_TO_COLOR = Color.Gray

@Composable
fun MailView(
    mail: Mail,
    onDelete: () -> Unit,
    onReply: () -> Unit,
    onClose: () -> Unit,
) {
    val appEnv = koinInject<AppEnv>()
    // Subject, From, To and Content
    SelectionContainer {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = mail.subject,
                fontSize = appEnv.MAIL_TITLE_FONT_SIZE.sp,
            )
            GeneralSpacer()
            Text(
                text = stringResource(Res.string.from, mail.sender),
                color = FROM_TO_COLOR,
            )
            GeneralSpacer(2.dp)
            Text(
                text = stringResource(Res.string.to, mail.receiver),
                color = FROM_TO_COLOR,
            )
            GeneralSpacer()
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Text(mail.content)
            }
        }
    }
    // Delete, Reply and Close
    AlignedBox(Alignment.TopEnd) {
        Column {
            // Close
            CloseButton(onClose)
            GeneralSpacer()
            // Reply
            IconButton(onReply) {
                GeneralIcon(
                    imageVector = TablerIcons.Send,
                    contentDescription = stringResource(Res.string.reply),
                )
            }
            GeneralSpacer()
            // Delete
            RemoveButton(onDelete)
        }
    }
}
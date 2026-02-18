package io.github.bommbomm34.intervirt.gui.intervirtos.mail.client

import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Send
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.content
import intervirt.ui.generated.resources.send
import intervirt.ui.generated.resources.sender
import intervirt.ui.generated.resources.subject
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun MailEditor(
    sender: MailUser,
    mail: Mail? = null,
    onCancel: () -> Unit = {},
    onSend: (Mail) -> Unit
) {
    var receiverAddress by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    AlignedBox(Alignment.TopStart) {
        CloseButton(onCancel)
    }
    CenterColumn {
        OutlinedTextField(
            value = sender.address,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.sender)) },
            singleLine = true
        )
        GeneralSpacer()
        OutlinedTextField(
            value = receiverAddress,
            onValueChange = { receiverAddress = it },
            label = { stringResource(Res.string.sender) },
            singleLine = true,
            isError = receiverAddress.validateMailAddress()
        )
        GeneralSpacer()
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { stringResource(Res.string.subject) },
            singleLine = true
        )
        GeneralSpacer(16.dp)
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text(stringResource(Res.string.content)) },
            minLines = 5
        )
    }
    AlignedBox(Alignment.BottomEnd){
        IconButton(
            onClick = {
                onSend(
                    Mail(
                        sender = sender,
                        receiver = receiverAddress.parse(),
                        subject = subject,
                        content = content
                    )
                )
            },
            enabled = receiverAddress.validateMailAddress()
        ){
            GeneralIcon(
                imageVector = TablerIcons.Send,
                contentDescription = stringResource(Res.string.send)
            )
        }
    }
}

private fun String.validateMailAddress() = count { it == '@' } == 1

private fun String.parse() = MailUser(substringBefore("@"), this)
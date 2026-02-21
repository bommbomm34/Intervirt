package io.github.bommbomm34.intervirt.intervirtos.mail.client

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.core.parseMailAddress
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.components.buttons.SendButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun MailEditor(
    sender: MailUser,
    mail: Mail? = null,
    onCancel: () -> Unit = {},
    onSend: (Mail) -> Unit,
) {
    var receiverAddress by remember { mutableStateOf(mail?.receiver?.address ?: "") }
    var subject by remember { mutableStateOf(mail?.subject ?: "") }
    var content by remember { mutableStateOf(mail?.content ?: "") }
    AlignedBox(Alignment.TopStart) {
        CloseButton(onCancel)
    }
    CenterColumn {
        OutlinedTextField(
            value = sender.address,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.sender)) },
            singleLine = true,
        )
        GeneralSpacer()
        OutlinedTextField(
            value = receiverAddress,
            onValueChange = { receiverAddress = it },
            label = { Text(stringResource(Res.string.receiver)) },
            singleLine = true,
            isError = !receiverAddress.validateMailAddress(),
        )
        GeneralSpacer()
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text(stringResource(Res.string.subject)) },
            singleLine = true,
        )
        GeneralSpacer(16.dp)
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text(stringResource(Res.string.content)) },
            minLines = 5,
        )
    }
    AlignedBox(Alignment.BottomEnd) {
        SendButton(receiverAddress.validateMailAddress()) {
            onSend(
                Mail(
                    sender = sender,
                    receiver = receiverAddress.parseMailAddress(),
                    subject = subject,
                    content = content,
                ),
            )
            receiverAddress = ""
            subject = ""
            content = ""
        }
    }
}

private fun String.validateMailAddress() = count { it == '@' } == 1

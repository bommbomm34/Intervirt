package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.core.data.getCommandResult

// TODO: Use SMTP/IMAP instead of terminal commands
class MailClientManager(
    bundle: ContainerClientBundle
) {
    private val ioClient = bundle.ioClient

    suspend fun sendMail(mail: Mail): Result<Unit> {
        return ioClient.exec(
            listOf(
                "sh", "-c",
                mail.sender.getSendMailCommand(mail)
            )
        ).mapCatching { flow ->
            flow.getCommandResult().asResult().getOrThrow()
        }
    }

    private fun MailUser.getSendMailCommand(mail: Mail) =
        """sudo -u $username echo "${mail.content}" | mail -s "${mail.subject}" ${mail.receiver.address}"""
}
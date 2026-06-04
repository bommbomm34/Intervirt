/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import io.github.bommbomm34.intervirt.core.exceptions.InvalidMailException
import jakarta.mail.Address
import jakarta.mail.Message
import jakarta.mail.Multipart
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

data class Mail(
    val sender: MailUser,
    val receiver: MailUser,
    val subject: String,
    val content: String,
    val index: Int? = null,
    val message: Message? = null,
) {
    fun getMessage(session: Session): Message = message ?: MimeMessage(session).apply {
        setFrom(InternetAddress(this@Mail.sender.address))
        setRecipient(Message.RecipientType.TO, InternetAddress(receiver.address))
        subject = this@Mail.subject
        setText(this@Mail.content)
    }
}

fun Message.toMail(index: Int? = null, senderOptional: Boolean = false): AppResult<Mail> = either {
    val sender = from?.get(0)?.toMailUser()
    val receiver = allRecipients?.get(0)?.toMailUser()
    if (sender == null && !senderOptional) return Failure.InvalidMail("No sender is specified").left()
    if (receiver == null) return Failure.InvalidMail("No receiver is specified").left()
    return Mail(
        sender = sender ?: MailUser.UNDEFINED,
        receiver = receiver,
        subject = subject,
        content = content.getString(),
        index = index,
        message = this@toMail,
    ).right()
}

private fun Any.getString(): String {
    when (this) {
        is String -> return this
        is Multipart -> {
            for (i in 0 until count) {
                val part = getBodyPart(i)
                return when {
                    part.isMimeType("text/plain") || part.isMimeType("text/html") -> part.content.toString()
                    part.content is Multipart -> getString()
                    else -> "[Unknown mime type ${part.contentType}]"
                }
            }
        }
    }
    return "[Empty mail]"
}

private fun Address.toMailUser(): MailUser? = if (this is InternetAddress) MailUser(
    username = personal ?: address,
    address = address,
) else null

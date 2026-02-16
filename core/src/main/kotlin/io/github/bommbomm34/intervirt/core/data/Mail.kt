package io.github.bommbomm34.intervirt.core.data

import io.github.bommbomm34.intervirt.core.exceptions.InvalidMailException
import jakarta.mail.Address
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

data class Mail(
    val sender: MailUser,
    val receiver: MailUser,
    val subject: String,
    val content: String,
    val index: Int? = null
) {
    fun getMimeMessage(session: Session): MimeMessage = MimeMessage(session).apply {
        setFrom(InternetAddress(this@Mail.sender.address))
        setRecipient(Message.RecipientType.TO, InternetAddress(receiver.address))
        subject = this@Mail.subject
        setText(this@Mail.content)
    }
}

fun Message.toMail(index: Int? = null): Result<Mail> {
    val sender = from[0].toMailUser()
    val receiver = allRecipients[0].toMailUser()
    if (sender == null) return Result.failure(InvalidMailException("No sender is specified"))
    if (receiver == null) return Result.failure(InvalidMailException("No receiver is specified"))
    return Result.success(
        Mail(
            sender = sender,
            receiver = receiver,
            subject = subject,
            content = content.toString(),
            index = index
        )
    )
}

private fun Address.toMailUser(): MailUser? = if (this is InternetAddress) MailUser(
    username = personal ?: address,
    address = address
) else null
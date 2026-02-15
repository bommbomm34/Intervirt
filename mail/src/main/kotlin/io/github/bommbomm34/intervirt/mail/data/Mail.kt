package io.github.bommbomm34.intervirt.mail.data

import uniffi.mail.MailBodyType
import uniffi.mail.NativeMail


data class Mail(
    val sender: String,
    val receiver: String,
    val subject: String,
    val bodyType: BodyType,
    val body: String
) {
    enum class BodyType { TEXT, HTML }

    fun BodyType.toNative() = when (this) {
        BodyType.TEXT -> MailBodyType.TEXT
        BodyType.HTML -> MailBodyType.HTML
    }

    fun toNative() = NativeMail(
        sender = sender,
        receiver = receiver,
        subject = subject,
        bodyType = bodyType.toNative(),
        body = body
    )
}
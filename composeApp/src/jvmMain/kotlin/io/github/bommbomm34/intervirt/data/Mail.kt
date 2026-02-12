package io.github.bommbomm34.intervirt.data

data class Mail(
    val sender: MailUser,
    val receiver: MailUser,
    val subject: String,
    val content: String
)

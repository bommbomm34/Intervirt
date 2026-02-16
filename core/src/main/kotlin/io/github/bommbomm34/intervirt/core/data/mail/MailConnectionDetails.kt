package io.github.bommbomm34.intervirt.core.data.mail

import io.github.bommbomm34.intervirt.core.data.Address

data class MailConnectionDetails(
    val smtpAddress: Address,
    val smtpSafety: MailConnectionSafety,
    val imapAddress: Address,
    val imapSafety: MailConnectionSafety,
    val username: String,
    val password: String
)



package io.github.bommbomm34.intervirt.core.data.mail

import io.github.bommbomm34.intervirt.core.data.Address

data class MailConnectionDetails(
    val smtpAddress: Address = Address.EXAMPLE,
    val smtpSafety: MailConnectionSafety = MailConnectionSafety.SECURE,
    val imapAddress: Address = Address.EXAMPLE,
    val imapSafety: MailConnectionSafety = MailConnectionSafety.SECURE,
    val username: String = "",
    val password: String = ""
)



package io.github.bommbomm34.intervirt.mail

import io.github.bommbomm34.intervirt.mail.data.Address
import io.github.bommbomm34.intervirt.mail.data.Mail
import uniffi.mail.NativeMailSender

/**
 * Class for sending mails via SMTP through an optional proxy.
 * Uses `lettre` from Rust
 */
class MailSender(
    private val host: Address,
    private val username: String? = null,
    private val password: String? = null,
    private val proxy: Address? = null
) {
    fun sendMail(mail: Mail): Result<Unit> {
        TODO("Not yet implemented")
    }
}
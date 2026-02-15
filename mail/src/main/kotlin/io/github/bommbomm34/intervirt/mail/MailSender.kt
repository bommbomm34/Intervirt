package io.github.bommbomm34.intervirt.mail

import io.github.bommbomm34.intervirt.mail.data.Address
import io.github.bommbomm34.intervirt.mail.data.Mail
import uniffi.mail.Greeter
import uniffi.mail.add

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
    fun sendMail(mail: Mail){
        Greeter("Hello").use { greeting ->
            println(greeting.greet("Rust"))
        }
    }
}
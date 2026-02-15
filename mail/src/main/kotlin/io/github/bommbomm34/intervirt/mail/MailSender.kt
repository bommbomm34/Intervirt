package io.github.bommbomm34.intervirt.mail

import io.github.bommbomm34.intervirt.mail.data.Address
import io.github.bommbomm34.intervirt.mail.data.Mail
import uniffi.mail.NativeMailSender
import uniffi.mail.SmtpCredentials

/**
 * Class for sending mails via SMTP through an optional proxy.
 * Uses `lettre` from Rust
 */
class MailSender(
    private val host: Address,
    private val username: String? = null,
    private val password: String? = null,
    private val proxy: Address? = null
) : AutoCloseable {
    private var nativeMailSender: NativeMailSender? = null

    /**
     * Initialize native mail sender. It returns a result of the initialization.
     * This is necessary before sending mails!
     */
    fun init(): Result<Unit> = runCatching {
        val credentials = if (username != null && password != null)
            SmtpCredentials(username, password) else null
        nativeMailSender = NativeMailSender(
            host = host.toNative(),
            credentials = credentials,
            proxy = proxy?.toNative()
        )
    }

    /**
     * Send mail [mail]. The receiver is taken from [mail].
     * It returns a result of sending the mail.
     * Throws IllegalStateException if mail sender hasn't been initialized.
     */
    fun sendMail(mail: Mail): Result<Unit> {
        if (nativeMailSender == null) error("MailSender hasn't been initialized!")
        return runCatching {
            nativeMailSender!!.sendMail(mail.toNative())
        }
    }

    override fun close() {
        nativeMailSender?.close()
    }
}
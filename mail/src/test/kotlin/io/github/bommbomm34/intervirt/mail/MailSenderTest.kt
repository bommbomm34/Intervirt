package io.github.bommbomm34.intervirt.mail

import io.github.bommbomm34.intervirt.mail.data.Address
import io.github.bommbomm34.intervirt.mail.data.Mail
import kotlin.test.Test

class MailSenderTest {
    @Test
    fun sendMailTest() {
        val exampleMail = Mail(
            sender = System.getenv("INTERVIRT_TEST_MAIL_SENDER"),
            receiver = System.getenv("INTERVIRT_TEST_MAIL_RECEIVER"),
            subject = "Hello World",
            bodyType = Mail.BodyType.TEXT,
            body = "Hello, this is a test message from the Intervirt.mail module"
        )
        val sender = MailSender(
            host = Address("127.0.0.1", 25)
        )
        val result = sender.sendMail(exampleMail)
        assert(result.isSuccess) { "Failed to send mail: ${result.exceptionOrNull()!!.stackTraceToString()}" }
    }
}
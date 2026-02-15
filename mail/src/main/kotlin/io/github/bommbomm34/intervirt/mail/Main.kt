package io.github.bommbomm34.intervirt.mail

import io.github.bommbomm34.intervirt.mail.data.Address
import io.github.bommbomm34.intervirt.mail.data.Mail

fun main(){
    val sender = MailSender(
        host = Address("0.0.0.0", 0)
    )
    sender.sendMail(Mail("", "", "", Mail.BodyType.TEXT, ""))
}
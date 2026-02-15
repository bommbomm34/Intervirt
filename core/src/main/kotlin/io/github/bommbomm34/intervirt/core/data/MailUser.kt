package io.github.bommbomm34.intervirt.core.data

data class MailUser(
    val username: String,
    val address: String
){
    override fun toString() = "$username <$address>"
}

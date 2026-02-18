package io.github.bommbomm34.intervirt.core.data

data class MailUser(
    val username: String,
    val address: String
){
    companion object {
        val UNDEFINED = MailUser("undefined", "undefined@undefined")
    }

    override fun toString() = "$username <$address>"
}

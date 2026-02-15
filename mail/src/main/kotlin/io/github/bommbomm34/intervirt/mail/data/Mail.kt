package io.github.bommbomm34.intervirt.mail.data

data class Mail(
    val sender: String,
    val receiver: String,
    val subject: String,
    val bodyType: BodyType,
    val body: String
){
    enum class BodyType { TEXT, HTML }
}
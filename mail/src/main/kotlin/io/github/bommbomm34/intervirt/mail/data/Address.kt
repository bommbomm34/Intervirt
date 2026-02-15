package io.github.bommbomm34.intervirt.mail.data

import uniffi.mail.NativeAddress


data class Address(
    val host: String,
    val port: Int
){
    fun toNative() = NativeAddress(host, port.toUShort())
}

package io.github.bommbomm34.intervirt.webview

import uniffi.composewebview_wry.ProxyConfig

data class Proxy(
    val host: String,
    val port: Int,
) {
    fun toConfig() = ProxyConfig(host, port.toUShort())
}

package io.github.bommbomm34.intervirt.data.dns

data class DnsRecord(
    val name: String,
    val ttl: Int,
    val dnsClass: String,
    val type: String,
    val data: String
)
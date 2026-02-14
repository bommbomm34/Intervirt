package io.github.bommbomm34.intervirt.core.data.dns

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DnsRecordAnswer(
    val name: String,
    val type: String,
    @SerialName("class")
    val dnsClass: String,
    val ttl: String,
    val address: String,
    val status: String,
    val nameserver: String
){
    fun toDnsRecord(): DnsRecord = DnsRecord(
        name = name,
        ttl = ttl.substringBefore("s").toInt(),
        dnsClass = dnsClass,
        type = type,
        data = address
    )
}
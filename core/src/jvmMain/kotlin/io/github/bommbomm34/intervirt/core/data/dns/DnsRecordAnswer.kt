/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

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
    val nameserver: String,
) {
    fun toDnsRecord(): DnsRecord = DnsRecord(
        name = name,
        ttl = parseTtl(ttl),
        dnsClass = dnsClass,
        type = type,
        data = address,
    )

    private fun parseTtl(str: String): Int {
        val lastLetter = str.last()
        val int = str.dropLast(1).toInt()

        return when (lastLetter) {
            's' -> int
            'm' -> int * 60
            'h' -> int * 3600
            else -> error("Invalid TTL: $str")
        }
    }
}

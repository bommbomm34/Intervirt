package io.github.bommbomm34.intervirt.core.data.dns

data class DnsRecord(
    val name: String,
    val ttl: Int,
    val dnsClass: String,
    val type: String,
    val data: String,
) {
    companion object {
        fun parse(text: String): DnsRecord {
            val splitted = text.split(" ")
            return DnsRecord(
                name = splitted[0],
                ttl = splitted[1].toInt(),
                dnsClass = splitted[2],
                type = splitted[3],
                data = splitted[4],
            )
        }
    }

    override fun toString() = "$name $ttl $dnsClass $type $data"
}
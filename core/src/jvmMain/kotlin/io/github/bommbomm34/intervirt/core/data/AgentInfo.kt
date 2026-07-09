package io.github.bommbomm34.intervirt.core.data

import inet.ipaddr.IPAddress


data class AgentInfo(
    val version: String,
    val ipv4Subnet: IPAddress,
    val ipv6Subnet: IPAddress,
) {

    init {
        requireNotNull(ipv4Subnet.networkPrefixLength) { "Expected network prefix on IPv4 subnet: $ipv4Subnet" }
        requireNotNull(ipv6Subnet.networkPrefixLength) { "Expected network prefix on IPv6 subnet: $ipv6Subnet" }
    }
}

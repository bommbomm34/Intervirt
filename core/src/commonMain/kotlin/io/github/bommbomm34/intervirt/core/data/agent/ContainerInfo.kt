package io.github.bommbomm34.intervirt.core.data.agent

import io.github.bommbomm34.intervirt.core.data.PortForwarding
import kotlinx.serialization.Serializable

@Serializable
data class ContainerInfo(
    val id: String,
    val ipv4: String,
    val ipv6: String,
    val mac: String,
    val internet: Boolean,
    val image: String,
    val portForwardings: List<PortForwarding> = listOf(),
    val running: Boolean = false,
)

typealias Network = List<String>
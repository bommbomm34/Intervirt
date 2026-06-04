package io.github.bommbomm34.intervirt.core.data

import kotlinx.serialization.Serializable

@Serializable
data class Network(
    val name: String,
    val devices: List<String>,
    val ipv4: String,
    val ipv6: String,
)

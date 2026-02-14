package io.github.bommbomm34.intervirt.core.data

import kotlinx.serialization.Serializable

@Serializable
data class PortForwarding(
    val protocol: String,
    val hostPort: Int,
    val guestPort: Int
)
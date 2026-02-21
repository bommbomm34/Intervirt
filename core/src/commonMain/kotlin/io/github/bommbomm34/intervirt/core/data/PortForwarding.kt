package io.github.bommbomm34.intervirt.core.data

import kotlinx.serialization.Serializable

@Serializable
data class PortForwarding(
    val protocol: String,
    val hostPort: Int,
    val guestPort: Int,
) {
    companion object {
        val DEFAULT = PortForwarding(
            protocol = "tcp",
            hostPort = 8080,
            guestPort = 80,
        )
    }

    override fun toString() = "$protocol:$hostPort:$guestPort"
}
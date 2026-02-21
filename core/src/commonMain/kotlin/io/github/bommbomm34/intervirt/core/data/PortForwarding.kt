package io.github.bommbomm34.intervirt.core.data

import kotlinx.serialization.Serializable

@Serializable
data class PortForwarding(
    val protocol: String,
    val externalPort: Int,
    val internalPort: Int,
) {
    companion object {
        val DEFAULT = PortForwarding(
            protocol = "tcp",
            externalPort = 8080,
            internalPort = 80,
        )
    }

    override fun toString() = "$protocol:$externalPort:$internalPort"
}
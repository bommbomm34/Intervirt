package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.api.AgentInterface
import kotlinx.serialization.Serializable

// Configuration of a Intervirt project
@Serializable
data class Configuration (
    val version: String,
    val author: String,
    val devices: MutableList<Device>
) {
    fun syncConfiguration(agentInterface: AgentInterface){
        // TODO: Apply devices in VM via API of the guest agent
    }
}
package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.AGENT_PORT
import io.github.bommbomm34.intervirt.VM_SHUTDOWN_TIMEOUT
import io.github.bommbomm34.intervirt.preferences
import java.io.File

data class AppConfigurationData(
    val vmShutdownTimeout: Int,
    val agentPort: Int,
    val intervirtFolder: String
)

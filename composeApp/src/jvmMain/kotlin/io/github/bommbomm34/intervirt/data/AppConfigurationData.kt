package io.github.bommbomm34.intervirt.data

data class AppConfigurationData(
    val vmShutdownTimeout: Int,
    val agentPort: Int,
    val intervirtFolder: String
)

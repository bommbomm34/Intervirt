package io.github.bommbomm34.intervirt.core.data

data class AppConfigurationData(
    val vmShutdownTimeout: Int,
    val agentPort: Int,
    val intervirtFolder: String,
    val darkMode: Boolean,
    val language: String
)

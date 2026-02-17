package io.github.bommbomm34.intervirt.core.data

import java.io.File
import java.util.*

data class AppEnv(
    val debugEnabled: Boolean,
    val agentTimeout: Long,
    val qemuMonitorTimeout: Long,
    val agentPort: Int,
    val vmShutdownTimeout: Long,
    val vmRam: Int,
    val vmCpu: Int,
    val vmEnableKvm: Boolean,
    val dataDir: File,
    val darkMode: Boolean?,
    val tooltipFontSize: Int,
    val connectionStrokeWidth: Float,
    val deviceConnectionColor: Long,
    val zoomSpeed: Float,
    val deviceSize: Int,
    val osIconSize: Int,
    val suggestedFilename: String,
    val language: Locale,
    val enableAgent: Boolean,
    val qemuMonitorPort: Int,
    val titleFontSize: Int,
    val appIconSize: Int?,
    val defaultDnsServer: String,
    val pseudoMode: Boolean,
    val enableJavaScript: Boolean,
    val virtualContainerIO: Boolean,
    val vmDiskUrl: String,
    val vmDiskHashUrl: String,
    val qemuZipUrl: String,
    val qemuZipHashUrl: String,
    val agentWebSocketTimeout: Long,
    val mailUsername: String,
    val mailPassword: String,
    val smtpServerAddress: String,
    val imapServerAddress: String
)

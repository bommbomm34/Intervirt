package io.github.bommbomm34.intervirt.data

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import java.io.File
import java.util.Locale

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
    val tooltipFontSize: TextUnit,
    val connectionStrokeWidth: Float,
    val deviceConnectionColor: Long,
    val zoomSpeed: Float,
    val deviceSize: Dp,
    val osIconSize: Dp,
    val suggestedFilename: String,
    val language: Locale,
    val enableAgent: Boolean,
    val qemuMonitorPort: Int,
    val titleFontSize: TextUnit,
    val appIconSize: Dp?,
    val defaultDnsServer: String
)

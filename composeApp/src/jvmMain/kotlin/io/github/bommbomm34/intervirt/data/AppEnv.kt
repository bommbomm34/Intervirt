package io.github.bommbomm34.intervirt.data

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.util.Locale

@Suppress("PropertyName")
data class AppEnv(
    val DEBUG_ENABLED: Boolean,
    val AGENT_TIMEOUT: Long,
    val QEMU_MONITOR_TIMEOUT: Long,
    val AGENT_PORT: Int,
    val VM_SHUTDOWN_TIMEOUT: Long,
    val VM_RAM: Int,
    val VM_CPU: Int,
    val VM_ENABLE_KVM: Boolean,
    val DATA_DIR: File,
    val DARK_MODE: Boolean?,
    val TOOLTIP_FONT_SIZE: TextUnit,
    val CONNECTION_STROKE_WIDTH: Float,
    val DEVICE_CONNECTION_COLOR: Long,
    val ZOOM_SPEED: Float,
    val DEVICE_SIZE: Dp,
    val OS_ICON_SIZE: Dp,
    val SUGGESTED_FILENAME: String,
    val LANGUAGE: Locale,
    val ENABLE_AGENT: Boolean,
    val QEMU_MONITOR_PORT: Int,
    val TITLE_FONT_SIZE: TextUnit,
    val APP_ICON_SIZE: Dp?,
    val DEFAULT_DNS_SERVER: String
)

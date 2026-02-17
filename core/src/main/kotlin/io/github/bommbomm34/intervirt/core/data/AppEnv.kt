package io.github.bommbomm34.intervirt.core.data

import io.github.bommbomm34.intervirt.core.api.Preferences
import java.io.File
import java.util.*

private val defaultQemuZipUrl = when (getOS()){
    OS.WINDOWS -> "https://cdn.perhof.org/bommbomm34/qemu/windows-portable.zip"
    OS.LINUX -> "https://cdn.perhof.org/bommbomm34/qemu/linux-portable.zip"
}

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

@Suppress("ClassName")
sealed class PreferenceAccessor<T> (private val producer: (String?) -> T) {
    private var initialized = false
    private var value: T? = null

    object DEBUG_ENABLED : PreferenceAccessor<Boolean>({ it.toBoolean() })
    object AGENT_TIMEOUT : PreferenceAccessor<Long>({ it?.toLong() ?: 30000L })
    object QEMU_MONITOR_TIMEOUT : PreferenceAccessor<Long>({ it?.toLong() ?: 5000L })
    object AGENT_PORT : PreferenceAccessor<Int>({ it?.toInt() ?: 55436 })
    object VM_SHUTDOWN_TIMEOUT : PreferenceAccessor<Long>({ it?.toLong() ?: 30000L })
    object VM_RAM : PreferenceAccessor<Int>({ it?.toInt() ?: 2048 })
    object VM_CPU : PreferenceAccessor<Int>({ it?.toInt() ?: 1 })
    object VM_ENABLE_KVM : PreferenceAccessor<Boolean>({ it?.toBoolean() ?: false })
    object DATA_DIR : PreferenceAccessor<File>({ File(it ?: (System.getProperty("user.home") + File.separator + "Intervirt")) })
    object DARK_MODE : PreferenceAccessor<Boolean?>({ it?.toBoolean() })
    object TOOLTIP_FONT_SIZE : PreferenceAccessor<Int>({ it?.toInt() ?: 12 })
    object CONNECTION_STROKE_WIDTH : PreferenceAccessor<Float>({ it?.toFloat() ?: 10f })
    object DEVICE_CONNECTION_COLOR : PreferenceAccessor<Long>({ it?.toLong() ?: 0xFF9CCC65 })
    object ZOOM_SPEED : PreferenceAccessor<Float>({ it?.toFloat() ?: 0.1f })
    object DEVICE_SIZE : PreferenceAccessor<Int>({ it?.toInt() ?: 100 })
    object OS_ICON_SIZE : PreferenceAccessor<Int>({ it?.toInt() ?: 128 })
    object SUGGESTED_FILENAME : PreferenceAccessor<String>({ it ?: "MyIntervirtProject" })
    object LANGUAGE : PreferenceAccessor<Locale>({ lang -> lang?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault() ?: Locale.US })
    object ENABLE_AGENT : PreferenceAccessor<Boolean>({ it?.toBooleanStrictOrNull() ?: true })
    object QEMU_MONITOR_PORT : PreferenceAccessor<Int>({ it?.toInt() ?: 55437 })
    object TITLE_FONT_SIZE : PreferenceAccessor<Int>({ it?.toInt() ?: 48 })
    object APP_ICON_SIZE : PreferenceAccessor<Int?>({ it?.toInt() })
    object DEFAULT_DNS_SERVER : PreferenceAccessor<String>({ it ?: "9.9.9.9" })
    object PSEUDO_MODE : PreferenceAccessor<Boolean>({ it.toBoolean() })
    object ENABLE_JAVA_SCRIPT : PreferenceAccessor<Boolean>({ it?.toBoolean() ?: true })
    object VIRTUAL_CONTAINER_IO : PreferenceAccessor<Boolean>({ it.toBoolean() })
    object VM_DISK_URL : PreferenceAccessor<String>({ it ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2" })
    object VM_DISK_HASH_URL : PreferenceAccessor<String>({ it ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2.sha256" })
    object QEMU_ZIP_URL : PreferenceAccessor<String>({ it ?: defaultQemuZipUrl })
    object QEMU_ZIP_HASH_URL : PreferenceAccessor<String>({ it ?: "$defaultQemuZipUrl.sha256" })
    object AGENT_WEB_SOCKET_TIMEOUT : PreferenceAccessor<Long>({ it?.toLong() ?: 10_000L })
    object MAIL_USERNAME : PreferenceAccessor<String>({ it ?: "" })
    object MAIL_PASSWORD : PreferenceAccessor<String>({ it ?: "" })
    object SMTP_SERVER_ADDRESS : PreferenceAccessor<String>({ it ?: "" })
    object IMAP_SERVER_ADDRESS : PreferenceAccessor<String>({ it ?: "" })

    fun get(getEnv: (String) -> (String?)): T {
        if (!initialized) value = producer(getEnv(this::class.simpleName!!))
        return value!!
    }
}
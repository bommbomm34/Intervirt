package io.github.bommbomm34.intervirt.core.data

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

data class AppEnv(
    private val env: (String) -> String?,
    private val save: (String, String) -> Unit,
    private val custom: AppEnv.() -> Unit = {}
) {
    private val defaultQemuZipUrl = when (getOS()) {
        OS.WINDOWS -> "https://cdn.perhof.org/bommbomm34/qemu/windows-portable.zip"
        OS.LINUX -> "https://cdn.perhof.org/bommbomm34/qemu/linux-portable.zip"
    }

    val debugEnabled: Boolean by EnvDelegate("DEBUG_ENABLED", save) {
        env(it).toBoolean()
    }

    val agentTimeout: Long by EnvDelegate("AGENT_TIMEOUT", save) {
        env(it)?.toLong() ?: 30_000L
    }

    val qemuMonitorTimeout: Long by EnvDelegate("QEMU_MONITOR_TIMEOUT", save) {
        env(it)?.toLong() ?: 5_000L
    }

    val agentPort: Int by EnvDelegate("AGENT_PORT", save) {
        env(it)?.toInt() ?: 55436
    }

    val vmShutdownTimeout: Long by EnvDelegate("VM_SHUTDOWN_TIMEOUT", save) {
        env(it)?.toLong() ?: 30_000L
    }

    val vmRam: Int by EnvDelegate("VM_RAM", save) {
        env(it)?.toInt() ?: 2048
    }

    val vmCpu: Int by EnvDelegate("VM_CPU", save) {
        env(it)?.toInt() ?: 1
    }

    val vmEnableKvm: Boolean by EnvDelegate("VM_ENABLE_KVM", save) {
        env(it)?.toBoolean() ?: false
    }

    val dataDir: File by EnvDelegate("DATA_DIR", save) {
        File(env(it) ?: "${System.getProperty("user.home")}${File.separator}Intervirt")
    }

    val darkMode: Boolean? by EnvDelegate("DARK_MODE", save) {
        env(it)?.toBoolean()
    }

    val tooltipFontSize: Int by EnvDelegate("TOOLTIP_FONT_SIZE", save) {
        env(it)?.toInt() ?: 12
    }

    val connectionStrokeWidth: Float by EnvDelegate("CONNECTION_STROKE_WIDTH", save) {
        env(it)?.toFloat() ?: 10f
    }

    val deviceConnectionColor: Long by EnvDelegate("DEVICE_CONNECTION_COLOR", save) {
        env(it)?.toLong(16) ?: 0xFF9CCC65
    }

    val zoomSpeed: Float by EnvDelegate("ZOOM_SPEED", save) {
        env(it)?.toFloat() ?: 0.1f
    }

    val deviceSize: Int by EnvDelegate("DEVICE_SIZE", save) {
        env(it)?.toInt() ?: 100
    }

    val osIconSize: Int by EnvDelegate("OS_ICON_SIZE", save) {
        env(it)?.toInt() ?: 128
    }

    val suggestedFilename: String by EnvDelegate("SUGGESTED_FILENAME", save) {
        env(it) ?: "MyIntervirtProject"
    }

    val language: Locale by EnvDelegate("LANGUAGE", save) {
        env(it)?.let(Locale::forLanguageTag)
            ?: Locale.getDefault()
            ?: Locale.US
    }

    val enableAgent: Boolean by EnvDelegate("ENABLE_AGENT", save) {
        env(it)?.toBooleanStrictOrNull() ?: true
    }

    val qemuMonitorPort: Int by EnvDelegate("QEMU_MONITOR_PORT", save) {
        env(it)?.toInt() ?: 55_437
    }

    val titleFontSize: Int by EnvDelegate("TITLE_FONT_SIZE", save) {
        env(it)?.toInt() ?: 48
    }

    val appIconSize: Int? by EnvDelegate("APP_ICON_SIZE", save) {
        env(it)?.toInt()
    }

    val defaultDnsServer: String by EnvDelegate("DEFAULT_DNS_SERVER", save) {
        env(it) ?: "9.9.9.9"
    }

    val pseudoMode: Boolean by EnvDelegate("PSEUDO_MODE", save) {
        env(it).toBoolean()
    }

    val enableJavaScript: Boolean by EnvDelegate("ENABLE_JAVASCRIPT", save) {
        env(it)?.toBoolean() ?: true
    }

    val virtualContainerIO: Boolean by EnvDelegate("VIRTUAL_CONTAINER_IO", save) {
        env(it).toBoolean()
    }

    val vmDiskUrl: String by EnvDelegate("VM_DISK_URL", save) {
        env(it) ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2"
    }

    val vmDiskHashUrl: String by EnvDelegate("VM_DISK_HASH_URL", save) {
        env(it) ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2.sha256"
    }

    val qemuZipUrl: String by EnvDelegate("QEMU_ZIP_URL", save) {
        env(it) ?: defaultQemuZipUrl
    }

    val qemuZipHashUrl: String by EnvDelegate("QEMU_ZIP_HASH_URL", save) {
        env(it) ?: "$defaultQemuZipUrl.sha256"
    }

    val agentWebSocketTimeout: Long by EnvDelegate("AGENT_WEBSOCKET_TIMEOUT", save) {
        env(it)?.toLong() ?: 10_000L
    }

    val mailUsername: String by EnvDelegate("MAIL_USERNAME", save) {
        env(it) ?: ""
    }

    val mailPassword: String by EnvDelegate("MAIL_PASSWORD", save) {
        env(it) ?: ""
    }

    val smtpServerAddress: String by EnvDelegate("SMTP_SERVER_ADDRESS", save) {
        env(it) ?: ""
    }

    val imapServerAddress: String by EnvDelegate("IMAP_SERVER_ADDRESS", save) {
        env(it) ?: ""
    }
}

private class EnvDelegate<T>(
    private val env: String,
    private val save: (env: String, new: String) -> Unit,
    producer: (env: String) -> T
) {
    private var value = producer(env)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        save(env, value.toString())
        this.value = value
    }
}

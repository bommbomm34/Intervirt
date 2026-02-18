package io.github.bommbomm34.intervirt.core.data

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

data class AppEnv(
    private val env: (String) -> String?,
    private val save: (String, String) -> Unit,
    private val custom: AppEnv.() -> Unit = {},
) {
    private val defaultQemuZipUrl = when (getOS()) {
        OS.WINDOWS -> "https://cdn.perhof.org/bommbomm34/qemu/windows-portable.zip"
        OS.LINUX -> "https://cdn.perhof.org/bommbomm34/qemu/linux-portable.zip"
    }

    var debugEnabled: Boolean by EnvDelegate("DEBUG_ENABLED", save) {
        env(it).toBoolean()
    }

    var agentTimeout: Long by EnvDelegate("AGENT_TIMEOUT", save) {
        env(it)?.toLong() ?: 30_000L
    }

    var qemuMonitorTimeout: Long by EnvDelegate("QEMU_MONITOR_TIMEOUT", save) {
        env(it)?.toLong() ?: 5_000L
    }

    var agentPort: Int by EnvDelegate("AGENT_PORT", save) {
        env(it)?.toInt() ?: 55436
    }

    var vmShutdownTimeout: Long by EnvDelegate("VM_SHUTDOWN_TIMEOUT", save) {
        env(it)?.toLong() ?: 30_000L
    }

    var vmRam: Int by EnvDelegate("VM_RAM", save) {
        env(it)?.toInt() ?: 2048
    }

    var vmCpu: Int by EnvDelegate("VM_CPU", save) {
        env(it)?.toInt() ?: 1
    }

    var vmEnableKvm: Boolean by EnvDelegate("VM_ENABLE_KVM", save) {
        env(it)?.toBoolean() ?: false
    }

    var dataDir: File by EnvDelegate("DATA_DIR", save) {
        File(env(it) ?: "${System.getProperty("user.home")}${File.separator}Intervirt")
    }

    var darkMode: Boolean? by EnvDelegate("DARK_MODE", save) {
        env(it)?.toBoolean()
    }

    var tooltipFontSize: Int by EnvDelegate("TOOLTIP_FONT_SIZE", save) {
        env(it)?.toInt() ?: 12
    }

    var connectionStrokeWidth: Float by EnvDelegate("CONNECTION_STROKE_WIDTH", save) {
        env(it)?.toFloat() ?: 10f
    }

    var deviceConnectionColor: Long by EnvDelegate("DEVICE_CONNECTION_COLOR", save) {
        env(it)?.toLong(16) ?: 0xFF9CCC65
    }

    var zoomSpeed: Float by EnvDelegate("ZOOM_SPEED", save) {
        env(it)?.toFloat() ?: 0.1f
    }

    var deviceSize: Int by EnvDelegate("DEVICE_SIZE", save) {
        env(it)?.toInt() ?: 100
    }

    var osIconSize: Int by EnvDelegate("OS_ICON_SIZE", save) {
        env(it)?.toInt() ?: 128
    }

    var suggestedFilename: String by EnvDelegate("SUGGESTED_FILENAME", save) {
        env(it) ?: "MyIntervirtProject"
    }

    var language: Locale by EnvDelegate("LANGUAGE", save) {
        env(it)?.let(Locale::forLanguageTag)
            ?: Locale.getDefault()
            ?: Locale.US
    }

    var enableAgent: Boolean by EnvDelegate("ENABLE_AGENT", save) {
        env(it)?.toBooleanStrictOrNull() ?: true
    }

    var qemuMonitorPort: Int by EnvDelegate("QEMU_MONITOR_PORT", save) {
        env(it)?.toInt() ?: 55_437
    }

    var titleFontSize: Int by EnvDelegate("TITLE_FONT_SIZE", save) {
        env(it)?.toInt() ?: 48
    }

    var appIconSize: Int? by EnvDelegate("APP_ICON_SIZE", save) {
        env(it)?.toInt()
    }

    var defaultDnsServer: String by EnvDelegate("DEFAULT_DNS_SERVER", save) {
        env(it) ?: "9.9.9.9"
    }

    var pseudoMode: Boolean by EnvDelegate("PSEUDO_MODE", save) {
        env(it).toBoolean()
    }

    var enableJavaScript: Boolean by EnvDelegate("ENABLE_JAVASCRIPT", save) {
        env(it)?.toBoolean() ?: true
    }

    var virtualContainerIO: Boolean by EnvDelegate("VIRTUAL_CONTAINER_IO", save) {
        env(it).toBoolean()
    }

    var vmDiskUrl: String by EnvDelegate("VM_DISK_URL", save) {
        env(it) ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2"
    }

    var vmDiskHashUrl: String by EnvDelegate("VM_DISK_HASH_URL", save) {
        env(it) ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2.sha256"
    }

    var qemuZipUrl: String by EnvDelegate("QEMU_ZIP_URL", save) {
        env(it) ?: defaultQemuZipUrl
    }

    var qemuZipHashUrl: String by EnvDelegate("QEMU_ZIP_HASH_URL", save) {
        env(it) ?: "$defaultQemuZipUrl.sha256"
    }

    var agentWebSocketTimeout: Long by EnvDelegate("AGENT_WEBSOCKET_TIMEOUT", save) {
        env(it)?.toLong() ?: 10_000L
    }

    var mailTitleFontSize: Int by EnvDelegate("MAIL_TITLE_FONT_SIZE", save) {
        env(it)?.toInt() ?: 24
    }

    internal var diskInstalled: Boolean by EnvDelegate("DISK_INSTALLED", save) { env(it).toBoolean() }
    internal var currentDiskHash: String? by EnvDelegate("CURRENT_DISK_HASH", save) { env(it) }
    internal var qemuInstalled: Boolean by EnvDelegate("QEMU_INSTALLED", save) { env(it).toBoolean() }
    internal var currentQemuHash: String? by EnvDelegate("CURRENT_QEMU_HASH", save) { env(it) }
    var intervirtInstalled: Boolean by EnvDelegate("INSTALLED", save) { env(it).toBoolean() }
}

private class EnvDelegate<T>(
    private val env: String,
    private val save: (env: String, new: String) -> Unit,
    producer: (env: String) -> T,
) {
    private val value = producer(env)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = save(env, value.toString())
}

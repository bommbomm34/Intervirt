package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.OS
import io.github.bommbomm34.intervirt.core.data.Preference
import io.github.bommbomm34.intervirt.core.data.PreferenceAccessor
import io.github.bommbomm34.intervirt.core.data.getOS
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists

class Preferences {
    private val logger = KotlinLogging.logger {  }
    private val data = mutableMapOf<String, String>()
    private val dataFile: Path = File(System.getProperty("user.home") + File.separator + ".intervirt.config.json").toPath()
    private val defaultQemuZipUrl = when (getOS()){
        OS.WINDOWS -> "https://cdn.perhof.org/bommbomm34/qemu/windows-portable.zip"
        OS.LINUX -> "https://cdn.perhof.org/bommbomm34/qemu/linux-portable.zip"
    }

    init { load() }

    operator fun <T> get(accessor: PreferenceAccessor<T>) = accessor.get(::env)

    fun loadString(key: String): String? = data[key]

    fun saveString(key: String, value: String) {
        logger.debug { "Saving string $key with $value" }
        data[key] = value
        flush()
    }

    fun removeString(key: String){
        logger.debug { "Removing string $key" }
        data.remove(key)
        flush()
    }

    fun getAppEnv() = AppEnv(
        debugEnabled = env("DEBUG_ENABLED").toBoolean(),
        agentTimeout = env("AGENT_TIMEOUT")?.toLong() ?: 30000L,
        qemuMonitorTimeout = env("QEMU_MONITOR_TIMEOUT")?.toLong() ?: 5000L,
        agentPort = env("AGENT_PORT")?.toInt() ?: 55436,
        vmShutdownTimeout = env("VM_SHUTDOWN_TIMEOUT")?.toLong() ?: 30000L,
        vmRam = env("VM_RAM")?.toInt() ?: 2048,
        vmCpu = env("VM_CPU")?.toInt() ?: 1,
        vmEnableKvm = env("VM_ENABLE_KVM")?.toBoolean() ?: false,
        dataDir = File(env("DATA_DIR") ?: (System.getProperty("user.home") + File.separator + "Intervirt")),
        darkMode = env("DARK_MODE")?.toBoolean(),
        tooltipFontSize = env("TOOLTIP_FONT_SIZE")?.toInt() ?: 12,
        connectionStrokeWidth = env("CONNECTION_STROKE_WIDTH")?.toFloat() ?: 10f,
        deviceConnectionColor = env("DEVICE_CONNECTION_COLOR")?.toLong(16) ?: 0xFF9CCC65,
        zoomSpeed = env("ZOOM_SPEED")?.toFloat() ?: 0.1f,
        deviceSize = env("DEVICE_SIZE")?.toInt() ?: 100,
        osIconSize = env("OS_ICON_SIZE")?.toInt() ?: 128,
        suggestedFilename = env("SUGGESTED_FILENAME") ?: "MyIntervirtProject",
        language = env("LANGUAGE")?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault() ?: Locale.US,
        enableAgent = env("ENABLE_AGENT")?.toBooleanStrictOrNull() ?: true,
        qemuMonitorPort = env("QEMU_MONITOR_PORT")?.toInt() ?: 55437,
        titleFontSize = env("TITLE_FONT_SIZE")?.toInt() ?: 48,
        appIconSize = env("APP_ICON_SIZE")?.toInt(),
        defaultDnsServer = env("DEFAULT_DNS_SERVER") ?: "9.9.9.9",
        pseudoMode = env("PSEUDO_MODE").toBoolean(),
        enableJavaScript = env("ENABLE_JAVASCRIPT")?.toBoolean() ?: true,
        virtualContainerIO = env("VIRTUAL_CONTAINER_IO").toBoolean(),
        vmDiskUrl = env("VM_DISK_URL") ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2",
        vmDiskHashUrl = env("VM_DISK_HASH_URL") ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2.sha256",
        qemuZipUrl = env("QEMU_ZIP_URL") ?: defaultQemuZipUrl,
        qemuZipHashUrl = env("QEMU_ZIP_HASH_URL") ?: "$defaultQemuZipUrl.sha256",
        agentWebSocketTimeout = env("AGENT_WEBSOCKET_TIMEOUT")?.toLong() ?: 10_000L,
        mailUsername = env("MAIL_USERNAME") ?: "",
        mailPassword = env("MAIL_PASSWORD") ?: "",
        smtpServerAddress = env("SMTP_SERVER_ADDRESS") ?: "",
        imapServerAddress = env("IMAP_SERVER_ADDRESS") ?: "",
    )
    
    fun env(name: String): String? = System.getenv("INTERVIRT_$name") ?: loadString(name)

    private fun flush() = Files.writeString(dataFile, defaultJson.encodeToString(data))

    private fun load() {
        if (dataFile.exists()){
            data.clear()
            data.putAll(defaultJson.decodeFromString(Files.readString(dataFile)))
        }
    }
}
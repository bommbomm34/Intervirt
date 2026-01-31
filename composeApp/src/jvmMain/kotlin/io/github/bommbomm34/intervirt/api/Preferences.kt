package io.github.bommbomm34.intervirt.api

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.data.AppEnv
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists

class Preferences {
    private val logger = KotlinLogging.logger {  }
    private val data = mutableMapOf<String, String>()
    private val dataFile: Path = File(System.getProperty("user.home") + File.separator + ".intervirt.config.json").toPath()

    init { load() }

    fun loadString(key: String): String? = data[key]

    fun saveString(key: String, value: String) {
        logger.debug { "Saving string $key with $value" }
        data[key] = value
        flush()
    }

    fun getAppEnv() = AppEnv(
        DEBUG_ENABLED = env("DEBUG_ENABLED").toBoolean(),
        AGENT_TIMEOUT = env("AGENT_TIMEOUT")?.toLong() ?: 30000L,
        QEMU_MONITOR_TIMEOUT = env("QEMU_MONITOR_TIMEOUT")?.toLong() ?: 5000L,
        AGENT_PORT = env("AGENT_PORT")?.toInt() ?: 55436,
        VM_SHUTDOWN_TIMEOUT = env("VM_SHUTDOWN_TIMEOUT")?.toLong() ?: 30000L,
        VM_RAM = env("VM_RAM")?.toInt() ?: 2048,
        VM_CPU = env("VM_CPU")?.toInt() ?: 1,
        VM_ENABLE_KVM = env("VM_ENABLE_KVM")?.toBoolean() ?: false,
        DATA_DIR = File(env("DATA_DIR") ?: (System.getProperty("user.home") + File.separator + "Intervirt")),
        DARK_MODE = env("DARK_MODE")?.toBoolean(),
        TOOLTIP_FONT_SIZE = env("TOOLTIP_FONT_SIZE")?.toInt()?.sp ?: 12.sp,
        CONNECTION_STROKE_WIDTH = env("CONNECTION_STROKE_WIDTH")?.toFloat() ?: 10f,
        DEVICE_CONNECTION_COLOR = env("DEVICE_CONNECTION_COLOR")?.toLong(16) ?: 0xFF9CCC65,
        ZOOM_SPEED = env("ZOOM_SPEED")?.toFloat() ?: 0.1f,
        DEVICE_SIZE = env("DEVICE_SIZE")?.toInt()?.dp ?: 100.dp,
        OS_ICON_SIZE = env("OS_ICON_SIZE")?.toInt()?.dp ?: 128.dp,
        SUGGESTED_FILENAME = env("SUGGESTED_FILENAME") ?: "MyIntervirtProject",
        LANGUAGE = env("LANGUAGE")?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault() ?: Locale.US,
        ENABLE_AGENT = env("ENABLE_AGENT")?.toBooleanStrictOrNull() ?: true,
        QEMU_MONITOR_PORT = env("QEMU_MONITOR_PORT")?.toInt() ?: 55437,
        TITLE_FONT_SIZE = env("TITLE_FONT_SIZE")?.toInt()?.sp ?: 48.sp,
        APP_ICON_SIZE = env("APP_ICON_SIZE")?.toInt()?.dp,
        DEFAULT_DNS_SERVER = env("DEFAULT_DNS_SERVER") ?: "9.9.9.9"
    )
    
    fun env(name: String): String? = System.getenv("INTERVIRT_$name") ?: loadString(name)

    private fun flush() = Files.writeString(dataFile, Json.encodeToString(data))

    private fun load() {
        if (dataFile.exists()){
            data.clear()
            data.putAll(Json.Default.decodeFromString(Files.readString(dataFile)))
        }
    }
}
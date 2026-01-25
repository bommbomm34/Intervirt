package io.github.bommbomm34.intervirt.api

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.exists

@Suppress("PropertyName")
class Preferences {
    private val logger = KotlinLogging.logger {  }
    private val data = mutableMapOf<String, String>()
    private val dataFile: Path = File(System.getProperty("user.home") + File.separator + ".intervirt.config.json").toPath()

    val DEBUG_ENABLED = env("DEBUG_ENABLED").toBoolean()
    val AGENT_TIMEOUT = env("AGENT_TIMEOUT")?.toLong() ?: 30000L
    val QEMU_MONITOR_TIMEOUT = env("QEMU_MONITOR_TIMEOUT")?.toLong() ?: 5000L
    val AGENT_PORT = env("AGENT_PORT")?.toInt() ?: 55436
    val VM_SHUTDOWN_TIMEOUT = env("VM_SHUTDOWN_TIMEOUT")?.toLong() ?: 30000L
    val VM_RAM = env("VM_RAM")?.toInt() ?: 2048
    val VM_CPU = env("VM_CPU")?.toInt() ?: 1
    val VM_ENABLE_KVM = env("VM_ENABLE_KVM")?.toBoolean() ?: false
    val DATA_DIR = File(env("DATA_DIR") ?: (System.getProperty("user.home") + File.separator + "Intervirt"))
    val DARK_MODE = env("DARK_MODE")?.toBoolean()
    val TOOLTIP_FONT_SIZE = 12.sp
    val CONNECTION_STROKE_WIDTH = env("CONNECTION_STROKE_WIDTH")?.toFloat() ?: 10f
    val DEVICE_CONNECTION_COLOR = env("DEVICE_CONNECTION_COLOR")?.toLong(16) ?: 0xFF9CCC65
    val ZOOM_SPEED = env("ZOOM_SPEED")?.toFloat() ?: 0.1f
    val DEVICE_SIZE = env("DEVICE_SIZE")?.toInt()?.dp ?: 100.dp
    val OS_ICON_SIZE = env("OS_ICON_SIZE")?.toInt()?.dp ?: 128.dp
    val SUGGESTED_FILENAME = env("SUGGESTED_FILENAME") ?: "MyIntervirtProject"
    val LANGUAGE: Locale = env("LANGUAGE")?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault() ?: Locale.US
    val ENABLE_AGENT = env("ENABLE_AGENT")?.toBooleanStrictOrNull() ?: true
    val QEMU_MONITOR_PORT = env("QEMU_MONITOR_PORT")?.toInt() ?: 55437

    init { load() }

    fun loadString(key: String): String? = data[key]

    fun saveString(key: String, value: String) {
        logger.debug { "Saving string $key with $value" }
        data[key] = value
        flush()
    }

    fun env(name: String): String? = System.getenv("INTERVIRT_$name") ?: loadString(name)

    private fun flush() = Files.writeString(dataFile, Json.Default.encodeToString(data))

    private fun load() {
        if (dataFile.exists()){
            data.clear()
            data.putAll(Json.Default.decodeFromString(Files.readString(dataFile)))
        }
    }

}
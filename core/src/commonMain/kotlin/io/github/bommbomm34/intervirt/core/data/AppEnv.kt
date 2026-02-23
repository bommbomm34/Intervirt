package io.github.bommbomm34.intervirt.core.data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import io.github.bommbomm34.intervirt.core.toPrimitive
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("PropertyName")
data class AppEnv(
    private val settings: Settings,
    private val override: (String) -> String? = { null },
    private val autoFlush: Boolean = true,
    private val custom: AppEnv.() -> Unit = {},
) {
    private val logger = KotlinLogging.logger { }
    private val defaultQemuZipUrl = when (getOS()) {
        OS.WINDOWS -> "https://cdn.perhof.org/bommbomm34/qemu/windows-portable.zip"
        OS.LINUX -> "https://cdn.perhof.org/bommbomm34/qemu/linux-portable.zip"
    }

    var DEBUG_ENABLED: Boolean by delegate(false)

    var AGENT_TIMEOUT: Int by delegate(30000)

    var QEMU_MONITOR_TIMEOUT: Int by delegate(5000)

    var AGENT_PORT: Int by delegate(55436)

    var VM_SHUTDOWN_TIMEOUT: Long by delegate(30000)

    var VM_RAM: Int by delegate(2048)

    var VM_CPU: Int by delegate(1)

    var VM_ENABLE_KVM: Boolean by delegate(false)

    var DATA_DIR: File by delegate<String, File>(
        default = "${System.getProperty("user.home")}${File.separator}Intervirt",
        serializer = { it.absolutePath },
        deserializer = { File(it) },
    )

    var DARK_MODE: Boolean? by delegate(
        default = -1,
        serializer = { bool -> bool?.let { if (it) 1 else 0 } ?: -1 },
        deserializer = { if (it == -1) null else it == 1 },
    )

    var TOOLTIP_FONT_SIZE: Int by delegate(12)

    var CONNECTION_STROKE_WIDTH: Float by delegate(10f)

    var DEVICE_CONNECTION_COLOR: Long by delegate(0xFF9CCC65)

    var ZOOM_SPEED: Float by delegate(0.1f)

    var DEVICE_SIZE: Int by delegate(100)

    var OS_ICON_SIZE: Int by delegate(128)

    var SUGGESTED_FILENAME: String by delegate("MyIntervirtProject")

    var LANGUAGE: Locale by delegate(
        default = "en",
        serializer = { it.toLanguageTag() },
        deserializer = { Locale.forLanguageTag(it) },
    )

    var QEMU_MONITOR_PORT: Int by delegate(55437)

    var TITLE_FONT_SIZE: Int by delegate(48)

    var APP_ICON_SIZE: Int by delegate(48)

    var DEFAULT_DNS_SERVER: String by delegate("9.9.9.9")

    var VIRTUAL_AGENT_MODE: Boolean by delegate(false)

    var ENABLE_JAVASCRIPT: Boolean by delegate(true)

    var VIRTUAL_CONTAINER_IO: Boolean by delegate(false)

    var VIRTUAL_CONTAINER_IO_PORT: Int by delegate(22)

    var VM_DISK_URL: String by delegate("https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2")

    var VM_DISK_HASH_URL: String by delegate("https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2.sha256")

    var QEMU_ZIP_URL: String by delegate(defaultQemuZipUrl)

    var QEMU_ZIP_HASH_URL: String by delegate("$defaultQemuZipUrl.sha256")

    var AGENT_WEBSOCKET_TIMEOUT: Long by delegate(10_000L)

    var MAIL_TITLE_FONT_SIZE: Int by delegate(24)

    internal var DISK_INSTALLED: Boolean by delegate(false)

    internal var CURRENT_DISK_HASH: String by delegate("")

    internal var QEMU_INSTALLED: Boolean by delegate(false)

    internal var CURRENT_QEMU_HASH: String by delegate("")

    var INTERVIRT_INSTALLED: Boolean by delegate(false)
    var IMAGES_URL: String by delegate("https://perhof.org/intervirt/images.json")
    var ACCENT_COLOR: ULong by delegate(0xFF648042.toULong())
    var SMALL_FAB_SIZE: Int by delegate(32)

    init {
        custom()
    }

    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    private inline fun <reified T : Any, R> delegate(
        default: T,
        crossinline serializer: (R) -> T,
        crossinline deserializer: (T) -> R,
    ): ReadWriteProperty<AppEnv, R> =
        object : ReadWriteProperty<AppEnv, R> {
            private var value: T? = null

            override operator fun getValue(thisRef: AppEnv, property: KProperty<*>): R {
                if (value == null) value = getVar(property.name)
                return deserializer(value!!)
            }

            override operator fun setValue(thisRef: AppEnv, property: KProperty<*>, value: R) {
                logger.debug { "Setting ${property.name} to $value" }
                val serialized = serializer(value)
                settings.encodeValue(
                    key = property.name,
                    value = serialized,
                )
                this.value = serialized
            }

            private fun getVar(name: String): T {
                return override(name)?.toPrimitive() ?: settings.decodeValue(
                    key = name,
                    defaultValue = default,
                )
            }
        }

    private inline fun <reified T : Any> delegate(default: T) = delegate(
        default = default,
        serializer = { it },
        deserializer = { it },
    )
}
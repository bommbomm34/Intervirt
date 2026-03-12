/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import io.github.bommbomm34.intervirt.core.util.atomic
import io.github.bommbomm34.intervirt.core.util.ext.getDefaultStreams
import io.github.bommbomm34.intervirt.core.util.ext.toPrimitive
import io.github.bommbomm34.intervirt.logging.KLogger
import io.github.bommbomm34.intervirt.logging.LogLevel
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("PropertyName")
data class AppEnv(
    private val settings: Settings,
    private val override: (String) -> String? = { null },
    private val autoFlush: Boolean = true,
    private val onChange: () -> Unit = {},
    private val logLevel: LogLevel? = null,
    private val custom: AppEnv.() -> Unit = {},
) {
    private val logger = KLogger(AppEnv::class, logLevel ?: getLogLevel(), *getDefaultStreams())
    private val defaultQemuZipUrl = when (getOS()) {
        OS.WINDOWS -> "https://cdn.perhof.org/bommbomm34/qemu/windows-portable.zip"
        OS.LINUX -> "https://cdn.perhof.org/bommbomm34/qemu/linux-portable.zip"
    }
    private val onChanges = ConcurrentHashMap<String, () -> Unit>()
    private val properties = mutableListOf<EnvProperty<*>>()

    var DEBUG_ENABLED: Boolean by delegate(false)

    var AGENT_TIMEOUT: Int by delegate(30000)

    var QEMU_MONITOR_TIMEOUT: Int by delegate(5000)

    var AGENT_PORT: Int by delegate(55436)

    var VM_SHUTDOWN_TIMEOUT: Long by delegate(30000)

    var VM_RAM: Int by delegate(2048)

    var VM_CPU: Int by delegate(1)

    var VM_ENABLE_KVM: Boolean by delegate(false)

    var DATA_DIR: PlatformFile by delegate(
        default = "${System.getProperty("user.home")}${File.separator}Intervirt",
        serializer = { it.absolutePath() },
        deserializer = { PlatformFile(it) },
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

    var VIRTUAL_CONTAINER_IO: Boolean by delegate(false)

    var VIRTUAL_CONTAINER_IO_PORT: Int by delegate(22)
    var WIPE_VIRTUAL_ON_CLOSE: Boolean by delegate(false)
    var ENABLE_TEMP_FILE: Boolean by delegate(true)
    var OVERRIDE_DOCKER_HOST: String by delegate("")

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

    var INSTALLED: Boolean by delegate(false)
    var IMAGES_URL: String by delegate("https://raw.githubusercontent.com/bommbomm34/Intervirt/refs/heads/main/metadata/images.json")
    var ACCENT_COLOR: ULong by delegate(0xFF648042.toULong())
    var SMALL_FAB_SIZE: Int by delegate(32)
    var LOG_LEVEL: LogLevel by delegate(
        default = if (DEBUG_ENABLED) "DEBUG" else "ERROR",
        serializer = { it.toString() },
        deserializer = { LogLevel.valueOf(it) },
    )

    init {
        custom()
    }

    fun flush() = properties.forEach { it.flush() }

    fun addOnChange(name: String, block: () -> Unit) = onChanges.put(name, block)

    fun invalidateCache() = properties.forEach { it.invalidateCache() }

    override fun toString(): String = properties.joinToString("\n")

    private fun getLogLevel(): LogLevel {
        val severity = System.getenv("INTERVIRT_LOG_LEVEL")
            ?.let { LogLevel.valueOf(it) }
            ?: LogLevel.ERROR
        return severity
    }

    private inline fun <reified T : Any, R> delegate(
        default: T,
        noinline serializer: (R) -> T,
        noinline deserializer: (T) -> R,
    ) = EnvDelegateProvider(
        logger = logger,
        autoFlush = autoFlush,
        onChange = {
            onChange()
            onChanges[it]?.invoke()
        },
        settings = settings,
        override = override,
        addProperty = properties::add,
        default = default,
        serializer = serializer,
        deserializer = deserializer,
        tClass = T::class,
    )

    private inline fun <reified T : Any> delegate(default: T) = delegate(
        default = default,
        serializer = { it },
        deserializer = { it },
    )

}

private class EnvDelegateProvider<T : Any, R>(
    private val logger: KLogger,
    private val autoFlush: Boolean = true,
    private val onChange: (String) -> Unit,
    private val settings: Settings,
    private val override: (String) -> String?,
    private val addProperty: (EnvProperty<R>) -> Unit,
    private val default: T,
    private val serializer: (R) -> T,
    private val deserializer: (T) -> R,
    private val tClass: KClass<T>,
) {
    @OptIn(InternalSerializationApi::class)
    val clazzSerializer = tClass.serializer()

    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class, ExperimentalAtomicApi::class)
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadWriteProperty<AppEnv, R> {
        val name = property.name
        return object : ReadWriteProperty<AppEnv, R> {
            private var value: T? by atomic(null)

            init {
                addProperty(
                    EnvProperty(
                        name = name,
                        get = ::get,
                        flush = ::flush,
                        invalidateCache = ::invalidateCache,
                    ),
                )
            }

            override operator fun getValue(thisRef: AppEnv, property: KProperty<*>): R {
                return get()
            }

            override operator fun setValue(thisRef: AppEnv, property: KProperty<*>, value: R) {
                logger.debug { "Setting ${property.name} to $value" }
                val serialized = serializer(value)
                this.value = serialized
                if (autoFlush) flush()
                onChange(name)
            }

            fun flush() {
                value?.let { settings.encodeValue(clazzSerializer, name, it) }
            }

            fun invalidateCache() {
                value = null
            }

            private fun get(): R {
                if (value == null) value = getVar(name)
                return deserializer(value!!)
            }

            private fun getVar(name: String): T {
                return override(name)?.toPrimitive(tClass) ?: settings.decodeValue(
                    serializer = clazzSerializer,
                    key = name,
                    defaultValue = default,
                )
            }
        }
    }
}

private data class EnvProperty<T>(
    val name: String,
    val get: () -> T,
    val flush: () -> Unit,
    val invalidateCache: () -> Unit,
) {
    override fun toString() = "${name}=${get()}"
}
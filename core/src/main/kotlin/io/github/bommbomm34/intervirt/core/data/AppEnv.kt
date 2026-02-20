package io.github.bommbomm34.intervirt.core.data

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

@Suppress("PropertyName")
data class AppEnv(
    private val env: (String) -> String?,
    private val save: (String, String) -> Unit,
    private val delete: (String) -> Unit,
    private val custom: AppEnv.() -> Unit = {},
) {
    private val defaultQemuZipUrl = when (getOS()) {
        OS.WINDOWS -> "https://cdn.perhof.org/bommbomm34/qemu/windows-portable.zip"
        OS.LINUX -> "https://cdn.perhof.org/bommbomm34/qemu/linux-portable.zip"
    }

    var DEBUG_ENABLED: Boolean by delegate { it.toBoolean() }

    var AGENT_TIMEOUT: Long by delegate { it?.toLong() ?: 30_000L }

    var QEMU_MONITOR_TIMEOUT: Long by delegate { it?.toLong() ?: 5_000L }

    var AGENT_PORT: Int by delegate { it?.toInt() ?: 55436 }

    var VM_SHUTDOWN_TIMEOUT: Long by delegate { it?.toLong() ?: 30_000L }

    var VM_RAM: Int by delegate { it?.toInt() ?: 2048 }

    var VM_CPU: Int by delegate { it?.toInt() ?: 1 }

    var VM_ENABLE_KVM: Boolean by delegate { it?.toBoolean() ?: false }

    var DATA_DIR: File by delegate {
        File(it ?: "${System.getProperty("user.home")}${File.separator}Intervirt")
    }

    var DARK_MODE: Boolean? by delegate { it?.toBoolean() }

    var TOOLTIP_FONT_SIZE: Int by delegate { it?.toInt() ?: 12 }

    var CONNECTION_STROKE_WIDTH: Float by delegate { it?.toFloat() ?: 10f }

    var DEVICE_CONNECTION_COLOR: Long by delegate { it?.toLong(16) ?: 0xFF9CCC65 }

    var ZOOM_SPEED: Float by delegate { it?.toFloat() ?: 0.1f }

    var DEVICE_SIZE: Int by delegate { it?.toInt() ?: 100 }

    var OS_ICON_SIZE: Int by delegate { it?.toInt() ?: 128 }

    var SUGGESTED_FILENAME: String by delegate { it ?: "MyIntervirtProject" }

    var LANGUAGE: Locale by delegate {
        it?.let(Locale::forLanguageTag)
            ?: Locale.getDefault()
            ?: Locale.US
    }

    var ENABLE_AGENT: Boolean by delegate { it?.toBooleanStrictOrNull() ?: true }

    var QEMU_MONITOR_PORT: Int by delegate { it?.toInt() ?: 55_437 }

    var TITLE_FONT_SIZE: Int by delegate { it?.toInt() ?: 48 }

    var APP_ICON_SIZE: Int? by delegate { it?.toInt() }

    var DEFAULT_DNS_SERVER: String by delegate { it ?: "9.9.9.9" }

    var PSEUDO_MODE: Boolean by delegate { it.toBoolean() }

    var ENABLE_JAVASCRIPT: Boolean by delegate { it?.toBoolean() ?: true }

    var VIRTUAL_CONTAINER_IO: Boolean by delegate { it.toBoolean() }

    var VIRTUAL_CONTAINER_IO_PORT: Int by delegate { it?.toInt() ?: 22 }

    var VM_DISK_URL: String by delegate {
        it ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2"
    }

    var VM_DISK_HASH_URL: String by delegate {
        it ?: "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2.sha256"
    }

    var QEMU_ZIP_URL: String by delegate { it ?: defaultQemuZipUrl }

    var QEMU_ZIP_HASH_URL: String by delegate { it ?: "$defaultQemuZipUrl.sha256" }

    var AGENT_WEBSOCKET_TIMEOUT: Long by delegate { it?.toLong() ?: 10_000L }

    var MAIL_TITLE_FONT_SIZE: Int by delegate { it?.toInt() ?: 24 }

    internal var DISK_INSTALLED: Boolean by delegate { it.toBoolean() }

    internal var CURRENT_DISK_HASH: String? by delegate { it }

    internal var QEMU_INSTALLED: Boolean by delegate { it.toBoolean() }

    internal var CURRENT_QEMU_HASH: String? by delegate { it }

    var INTERVIRT_INSTALLED: Boolean by delegate { it.toBoolean() }
    var IMAGES_URL: String by delegate { it ?: "https://perhof.org/intervirt/images.json" }

    private fun <T> delegate(producer: (String?) -> T) = object {
        private var value: Any? = UNINITIALIZED

        @Suppress("UNCHECKED_CAST")
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (value == UNINITIALIZED) value = producer(env(property.name))
            return value as T
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            if (value != null) save(property.name, value.toString()) else delete(property.name)
        }
    }
}

private object UNINITIALIZED
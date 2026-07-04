@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package io.github.bommbomm34.intervirt.core.data.env

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import com.russhwolf.settings.set
import io.github.bommbomm34.intervirt.core.data.OS
import io.github.bommbomm34.intervirt.core.data.env.AppEnv.Companion.PRIMARY_CONSTRUCTOR
import io.github.bommbomm34.intervirt.core.data.env.AppEnv.Companion.PRIMARY_CONSTRUCTOR_VALUE_PARAMETERS
import io.github.bommbomm34.intervirt.core.data.getOS
import io.github.bommbomm34.intervirt.core.util.ext.toPrimitive
import io.github.bommbomm34.intervirt.logging.LogLevel
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

private val defaultQemuZipUrl = when (getOS()) {
    OS.WINDOWS -> "https://cdn.perhof.org/bommbomm34/qemu/windows-portable.zip"
    OS.LINUX -> "https://cdn.perhof.org/bommbomm34/qemu/linux-portable.zip"
}

// TODO: Make AppEnv changes more reactive
data class AppEnv(
    @Env("DEBUG_ENABLED")
    val debugEnabled: Boolean = false,
    @Env("AGENT_TIMEOUT")
    val agentTimeout: Int = 30_000,
    @Env("QEMU_MONITOR_TIMEOUT")
    val qemuMonitorTimeout: Int = 5_000,
    @Env("AGENT_HOST")
    val agentHost: String = "localhost",
    @Env("AGENT_PORT")
    val agentPort: Int = 55436,
    @Env("VM_SHUTDOWN_TIMEOUT")
    val vmShutdownTimeout: Long = 30_000L,
    @Env("VM_RAM")
    val vmRam: Int = 2048,
    @Env("VM_CPU")
    val vmCpu: Int = 1,
    @Env("VM_ENABLE_KVM")
    val vmEnableKvm: Boolean = false,
    @Env("DATA_DIR")
    val dataDir: String = "${System.getProperty("user.home")}${File.separator}Intervirt",
    @Env("DARK_MODE")
    val darkMode: Boolean? = null,
    @Env("TOOLTIP_FONT_SIZE")
    val tooltipFontSize: Int = 12,
    @Env("CONNECTION_STROKE_WIDTH")
    val connectionStrokeWidth: Float = 10f,
    @Env("DEVICE_CONNECTION_COLOR")
    val deviceConnectionColor: Long = 0xFF9CCC65,
    @Env("ZOOM_SPEED")
    val zoomSpeed: Float = 0.1f,
    @Env("DEVICE_SCALE")
    val deviceScale: Float = 3f,
    @Env("OS_ICON_SIZE")
    val osIconSize: Int = 128,
    @Env("SUGGESTED_FILENAME")
    val suggestedFilename: String = "MyIntervirtProject",
    @Env("LANGUAGE")
    val language: String = "en",
    @Env("QEMU_MONITOR_PORT")
    val qemuMonitorPort: Int = 55437,
    @Env("TITLE_FONT_SIZE")
    val titleFontSize: Int = 48,
    @Env("DEFAULT_DNS_SERVER")
    val defaultDnsServer: String = "9.9.9.9",
    @Env("VIRTUAL_AGENT_MODE")
    val virtualAgentMode: Boolean = false,
    @Env("VIRTUAL_CONTAINER_IO")
    val virtualContainerIO: Boolean = false,
    @Env("VIRTUAL_DOCKER_MANAGER")
    val virtualDockerManager: Boolean = false,
    @Env("VIRTUAL_CONTAINER_IO_PORT")
    val virtualContainerIOPort: Int = 22,
    @Env("WIPE_VIRTUAL_ON_CLOSE")
    val wipeVirtualOnClose: Boolean = false,
    @Env("ENABLE_TEMP_FILE")
    val enableTempFile: Boolean = true,
    @Env("OVERRIDE_DOCKER_HOST")
    val overrideDockerHost: String = "",
    @Env("VM_DISK_URL")
    val vmDiskUrl: String = "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2",
    @Env("VM_DISK_HASH_URL")
    val vmDiskHashUrl: String = "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2.sha256",
    @Env("QEMU_ZIP_URL")
    val qemuZipUrl: String = defaultQemuZipUrl,
    @Env("QEMU_ZIP_HASH_URL")
    val qemuZipHashUrl: String = "$defaultQemuZipUrl.sha256",
    @Env("AGENT_WEBSOCKET_TIMEOUT")
    val agentWebsocketTimeout: Long = 10_000L,
    @Env("MAIL_TITLE_FONT_SIZE")
    val mailTitleFontSize: Int = 24,
    @Env("INSTALLED")
    val installed: Boolean = false,
    @Env("ACCENT_COLOR")
    val accentColor: ULong = 0xFF648042u,
    @Env("SMALL_FAB_SIZE")
    val smallFabSize: Int = 32,
    @Env("LOG_LEVEL")
    val logLevel: String = if (debugEnabled) "DEBUG" else "ERROR",
    @NoEnv
    val diskInstalled: Boolean = false,
    @NoEnv
    val currentDiskHash: String = "",
    @NoEnv
    val qemuInstalled: Boolean = false,
    @NoEnv
    val currentQemuHash: String = "",
) {
    companion object {
        internal val PRIMARY_CONSTRUCTOR = AppEnv::class.primaryConstructor!!
        internal val PRIMARY_CONSTRUCTOR_VALUE_PARAMETERS = PRIMARY_CONSTRUCTOR.valueParameters
        private val PROPERTIES = AppEnv::class.declaredMemberProperties
        val PRIMARY_CONSTRUCTOR_PROPERTIES = PROPERTIES.filter { property ->
            PRIMARY_CONSTRUCTOR_VALUE_PARAMETERS.any { it.name == property.name }
        }
    }

    val actualLogLevel: LogLevel = LogLevel.valueOf(logLevel)

    val actualDataDir: PlatformFile = PlatformFile(dataDir)
}

fun Settings.loadEnv(override: (String) -> String? = { null }): AppEnv {
    val map = mutableMapOf<KParameter, Any?>()
    for (param in PRIMARY_CONSTRUCTOR_VALUE_PARAMETERS) {
        when (val annotation = param.annotations.singleOrNull()) {
            is Env -> {
                val envVar = override(annotation.name)?.toPrimitive(param.type.classifier as KClass<*>)
                envVar?.let {
                    map[param] = it
                    continue
                }
                val name = param.name!!
                // Get of settings
                if (!hasKey(name)) continue
                // decodeValueOrNull can't return null because of the precondition hasKey
                map[param] = decodeValueOrNull(serializer(param.type), name)
            }
            is NoEnv -> {
                val name = param.name!!
                if (!hasKey(name)) continue
                // decodeValueOrNull can't return null because of the precondition hasKey
                map[param] = decodeValueOrNull(serializer(param.type), name)
            }
            else -> error("Expected either Env or NoEnv annotation on AppEnv parameter '$param'")
        }
    }
    return PRIMARY_CONSTRUCTOR.callBy(map)
}

fun Settings.storeEnv(env: AppEnv) {
    for (property in AppEnv.PRIMARY_CONSTRUCTOR_PROPERTIES) {
        encodeValue(serializer(property.returnType), property.name, property(env))
    }
}

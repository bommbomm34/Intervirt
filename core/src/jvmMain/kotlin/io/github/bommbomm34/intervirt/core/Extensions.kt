/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.exceptions.OperationAlreadyPerformedException
import io.github.bommbomm34.intervirt.core.util.ListOutputStream
import io.github.bommbomm34.intervirt.logging.LogLevel
import io.github.bommbomm34.intervirt.logging.getDefaultStream
import io.github.bommbomm34.intervirt.secret.SecretService
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.write
import io.github.vinceglb.filekit.writeString
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.prefs.Preferences
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.absolutePathString
import kotlin.math.pow
import kotlin.math.round
import kotlin.reflect.KClass
import kotlin.stackTraceToString
import kotlin.system.exitProcess
import kotlin.time.Clock

fun getAppEnv(
    settings: Settings = PreferencesSettings(Preferences.userRoot()),
    logLevel: LogLevel? = null,
    custom: AppEnv.() -> Unit = {},
) = AppEnv(
    settings = settings,
    override = { System.getenv("INTERVIRT_$it") },
    custom = custom,
    logLevel = logLevel,
)

fun getHttpClient(): HttpClient = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
}

fun getTestAppEnv(settings: Settings = MapSettings()) = getAppEnv(settings, LogLevel.DEBUG) {
    DEBUG_ENABLED = true
    VIRTUAL_AGENT_MODE = System.getenv("INTERVIRT_TEST_VIRTUAL_AGENT_MODE")?.toBoolean() ?: true
    VIRTUAL_CONTAINER_IO = System.getenv("INTERVIRT_TEST_VIRTUAL_CONTAINER_IO")?.toBoolean() ?: true
    EXPERIMENTAL_SSH_GUEST_MODE = System.getenv("INTERVIRT_TEST_EXPERIMENTAL_SSH_GUEST_MODE").toBoolean()
    DATA_DIR = PlatformFile(Files.createTempDirectory("intervirt-test").absolutePathString())
}

val totalDiskSpace: Long
    get() = FileSystems.getDefault()
        .rootDirectories
        .sumOf { Files.getFileStore(it).totalSpace }

val usableDiskSpace: Long
    get() = FileSystems.getDefault()
        .rootDirectories
        .sumOf { Files.getFileStore(it).usableSpace }

val unixTimestamp: Long
    get() = Clock.System.now().epochSeconds

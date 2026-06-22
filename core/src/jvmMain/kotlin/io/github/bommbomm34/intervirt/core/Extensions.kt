/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core

import arrow.optics.Lens
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.bommbomm34.intervirt.core.util.toAtomic
import io.github.bommbomm34.intervirt.logging.KLogger
import io.github.bommbomm34.intervirt.logging.LogLevel
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transformWhile
import kotlinx.serialization.json.Json
import org.koin.core.module.KoinDslMarker
import org.koin.core.module.Module
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.prefs.Preferences
import kotlin.io.path.absolutePathString
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
        contentConverter = KotlinxWebsocketSerializationConverter(defaultJson)
    }
}

fun getTestAppEnv(settings: Settings = MapSettings()) = getAppEnv(settings, LogLevel.DEBUG) {
    DEBUG_ENABLED = true
    VIRTUAL_AGENT_MODE = System.getenv("INTERVIRT_TEST_VIRTUAL_AGENT_MODE")?.toBoolean() ?: true
    VIRTUAL_CONTAINER_IO = System.getenv("INTERVIRT_TEST_VIRTUAL_CONTAINER_IO")?.toBoolean() ?: true
    DATA_DIR = PlatformFile(Files.createTempDirectory("intervirt-test").absolutePathString())
    LOG_LEVEL = LogLevel.DEBUG
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

fun Module.singleProject() = single { Project().toAtomic() }

fun <T> Flow<T>.takeWhileInclusive(predicate: suspend (T) -> Boolean) = transformWhile {
    emit(it)
    predicate(it)
}

fun KLogger.error(failure: Failure, output: () -> Any) {
    error { "$failure: ${output()}" }
}

fun <S, A> Lens<S, A>.modify(atomic: Atomic<S>, transform: (A) -> A): S {
    return atomic.updateAndGet { src ->
        modify(src) { transform(it) }
    }
}

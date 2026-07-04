/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core

import arrow.core.raise.Raise
import arrow.core.raise.recover
import arrow.optics.Lens
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import io.github.bommbomm34.intervirt.core.api.AppEnvUpdater
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.env.loadEnv
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
import kotlinx.coroutines.flow.transformWhile
import org.koin.core.module.Module
import org.koin.dsl.bind
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.prefs.Preferences
import kotlin.io.path.absolutePathString
import kotlin.time.Clock

fun getAppEnv(
    settings: Settings = PreferencesSettings(Preferences.userRoot()),
) = settings.loadEnv { System.getenv("INTERVIRT_$it") }

fun getHttpClient(): HttpClient = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(defaultJson)
    }
}

fun getTestAppEnv(settings: Settings = MapSettings()) = getAppEnv(settings).copy(
    debugEnabled = true,
    virtualAgentMode = System.getenv("INTERVIRT_TEST_VIRTUAL_AGENT_MODE")?.toBoolean() ?: true,
    virtualContainerIO = System.getenv("INTERVIRT_TEST_VIRTUAL_CONTAINER_IO")?.toBoolean() ?: true,
    dataDir = Files.createTempDirectory("intervirt-test").absolutePathString(),
    logLevel = "DEBUG",
)

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

fun Module.singleSettings() = single<Settings> { PreferencesSettings(Preferences.userRoot()) }

fun Module.singleTestSettings() = single<Settings> { MapSettings() }

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

inline fun <R, E> getOrNull(block: context(Raise<E>) () -> R): R? {
    return recover(
        block = block,
        recover = { null },
    )
}

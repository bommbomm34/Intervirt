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

suspend inline fun <T> runSuspendingCatching(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        e.printStackTrace()
        Result.failure(e)
    }
}

fun String.result() = Result.success(this)

fun <T> Exception.result() = Result.failure<T>(this)
fun Float.readablePercentage() = "${(times(100f)).roundBy()}%"
fun Float.roundBy(num: Int = 2): Float {
    val factor = 10f.pow(num)
    return round(times(factor)) / factor
}

fun String.parseMailAddress() =
    MailUser(substringBefore("@"), this)


fun <T> List<T>.addFirst(element: T): List<T> {
    val mutableList = toMutableList()
    mutableList.addFirst(element)
    return mutableList
}

fun <T> List<T>.patch(element1: T, element2: T) = map { if (it == element1) element2 else it }

fun String.parseAddress() = Address(
    substringBefore(":"),
    substringAfter(":").toInt(),
)

suspend fun <T> withCatchingContext(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T,
): Result<T> = withContext(context) {
    runSuspendingCatching {
        block()
    }
}

fun ByteArray.zeroize() = fill(0)

@Suppress("UNCHECKED_CAST")
fun <T : Any> String.toPrimitive(clazz: KClass<T>): T = when (clazz) {
    String::class -> this
    Int::class -> toInt()
    Long::class -> toLong()
    ULong::class -> toULong()
    Boolean::class -> toBoolean()
    Float::class -> toFloat()
    else -> throw SerializationException("${clazz.qualifiedName} is not supported!")
} as T

suspend fun <T> Flow<ResultProgress<T>>.lastResult() = (last() as ResultProgress.Result).result

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

fun <T> Flow<T>.catchTimeout(action: suspend FlowCollector<T>.() -> Unit) = catch {
    if (it is TimeoutCancellationException) action() else throw it
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



fun <T> flowCatching(block: suspend FlowCollector<ResultProgress<T>>.() -> Unit) = flow(block).catch {
    if (it is CancellationException) throw it else emit(ResultProgress.failure(it))
}

fun Result<Unit>.recoverAlreadyPerformed(): Result<Unit> = recoverCatching {
    if (it is OperationAlreadyPerformedException) Unit else throw it
}

fun String.toReadableImage() = when {
    startsWith("debian/") -> "Debian"
    startsWith("ubuntu/") -> "Ubuntu"
    startsWith("intervirtos/") -> "IntervirtOS"
    startsWith("almalinux/") -> "AlmaLinux"
    startsWith("alpine/") -> "Alpine Linux"
    startsWith("archlinux/") -> "Arch Linux"
    startsWith("centos/") -> "CentOS"
    startsWith("fedora/") -> "Fedora"
    startsWith("gentoo/") -> "Gentoo"
    startsWith("kali/") -> "Kali Linux"
    startsWith("mint/") -> "Linux Mint"
    startsWith("nixos/") -> "NixOS"
    startsWith("opensuse/") -> "openSUSE"
    startsWith("voidlinux/") -> "Void Linux"
    else -> null
}

fun getTestAppEnv(settings: Settings = MapSettings()) = getAppEnv(settings, LogLevel.DEBUG) {
    DEBUG_ENABLED = true
    VIRTUAL_AGENT_MODE = System.getenv("INTERVIRT_TEST_VIRTUAL_AGENT_MODE")?.toBoolean() ?: true
    VIRTUAL_CONTAINER_IO = System.getenv("INTERVIRT_TEST_VIRTUAL_CONTAINER_IO")?.toBoolean() ?: true
    DATA_DIR = PlatformFile(Files.createTempDirectory("intervirt-test").absolutePathString())
}

suspend fun PlatformFile.createFile() = write(byteArrayOf(0))

fun PlatformFile.toJavaPath(): Path = file.toPath()

package io.github.bommbomm34.intervirt.core

import com.russhwolf.settings.PreferencesSettings
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.prefs.Preferences
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow
import kotlin.math.round

suspend inline fun <T> runSuspendingCatching(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
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

inline fun <reified T> String.toPrimitive(): T = when (T::class) {
    String::class -> this
    Int::class -> toInt()
    Long::class -> toLong()
    ULong::class -> toULong()
    Boolean::class -> toBoolean()
    Float::class -> toFloat()
    else -> throw SerializationException("${T::class.qualifiedName} is not supported!")
} as T

suspend fun <T> Flow<ResultProgress<T>>.lastResult() = (last() as ResultProgress.Result).result

fun getAppEnv(custom: AppEnv.() -> Unit = {}) = AppEnv(
    settings = PreferencesSettings(Preferences.userRoot()),
    override = { System.getenv("INTERVIRT_$it") },
    custom = custom,
)

fun getHttpClient(): HttpClient = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
}
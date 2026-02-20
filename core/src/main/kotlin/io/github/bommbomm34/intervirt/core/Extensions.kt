package io.github.bommbomm34.intervirt.core

import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
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

fun String.parseMailAddress() = MailUser(substringBefore("@"), this)


fun <T> List<T>.addFirst(element: T): List<T> {
    val mutableList = toMutableList()
    mutableList.addFirst(element)
    return mutableList
}

fun String.parseAddress() = Address(substringBefore(":"), substringAfter(":").toInt())

suspend fun <T> withCatchingContext(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): Result<T> = withContext(context){
    runSuspendingCatching {
        block()
    }
}

fun ByteArray.zeroize() = fill(0)
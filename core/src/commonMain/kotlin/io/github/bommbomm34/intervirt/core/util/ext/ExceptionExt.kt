/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util.ext

import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.exceptions.OperationAlreadyPerformedException
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

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

fun <T> Exception.result() = Result.failure<T>(this)

suspend fun <T> withCatchingContext(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T,
): Result<T> = withContext(context) {
    runSuspendingCatching {
        block()
    }
}

fun Result<Unit>.recoverAlreadyPerformed(): Result<Unit> = recoverCatching {
    if (it is OperationAlreadyPerformedException) Unit else throw it
}

fun <T> flowCatching(block: suspend FlowCollector<ResultProgress<T>>.() -> Unit) = flow(block).catch {
    if (it is CancellationException) throw it else emit(ResultProgress.failure(it))
}

fun <T> Flow<T>.catchTimeout(action: suspend FlowCollector<T>.() -> Unit) = catch {
    if (it is TimeoutCancellationException) action() else throw it
}

suspend fun <T> Flow<ResultProgress<T>>.lastResult() = (last() as ResultProgress.Result).result

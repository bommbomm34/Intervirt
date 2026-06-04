/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util.ext

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.recover
import arrow.core.right
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.Failure
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

suspend fun <T> withCatchingContext(
    context: CoroutineContext,
    block: suspend context(Raise<Failure>) CoroutineScope.() -> T,
): AppResult<T> = withContext(context) {
    either {
        block()
    }
}

fun AppResult<Unit>.recoverAlreadyPerformed(): AppResult<Unit> = recover {
    if (it is Failure.OperationAlreadyPerformed) Unit else raise(it)
}

fun <T> Flow<T>.catchTimeout(action: suspend FlowCollector<T>.() -> Unit) = catch {
    if (it is TimeoutCancellationException) action() else throw it
}

suspend fun <T> Flow<ResultProgress<T>>.lastResult() = (last() as ResultProgress.Result).result

fun <T> Result<T>.toAppResult(): AppResult<T> = fold(
    onSuccess = { it.right() },
    onFailure = { Failure.Unexpected(it).left() }
)

fun <T> AppResult<T>.getOrThrow(): T = getOrElse { throw IllegalStateException("Failed: ${it.message}") }

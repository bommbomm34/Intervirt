/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util.ext

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.logging.OutputStream
import io.github.bommbomm34.intervirt.logging.getDefaultStream
import io.github.bommbomm34.intervirt.logging.printlnErr
import jakarta.mail.Transport.send
import jdk.jfr.internal.OldObjectSample.emit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

context(_: Raise<Failure>)
suspend fun <R> withCatchingContext(context: CoroutineContext, block: suspend CoroutineScope.() -> R): R {
    return withContext(context) {
        Failure.catch {
            block()
        }.bind()
    }
}

fun <T> flowCatching(
    block: suspend context(Raise<Failure>) FlowCollector<ResultProgress<T>>.() -> Unit,
): Flow<ResultProgress<T>> {
    return flow {
        var failure: Failure? = null
        recover(
            block = { block() },
            recover = { failure = it },
        )
        failure?.let { emit(ResultProgress.failure(it)) }
    }
}

fun <T> channelFlowCatching(
    block: suspend context(Raise<Failure>) ProducerScope<ResultProgress<T>>.() -> Unit,
): Flow<ResultProgress<T>> {
    return channelFlow {
        var failure: Failure? = null
        recover(
            block = { block() },
            recover = { failure = it },
        )
        failure?.let { send(ResultProgress.failure(it)) }
    }
}

fun <T> Flow<T>.catchTimeout(action: suspend FlowCollector<T>.() -> Unit) = catch {
    if (it is TimeoutCancellationException) action() else throw it
}

suspend fun <T> Flow<ResultProgress<T>>.lastResult() = (last() as ResultProgress.Result).result

context(_: Raise<Failure>)
fun <T> Result<T>.bind(): T = fold(
    onSuccess = { it },
    onFailure = { raise(Failure.Unexpected(it)) },
)

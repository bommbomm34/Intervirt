/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
data class MutexVar<T>(val value: T){
    @Transient val mutex = Mutex()

    @OptIn(ExperimentalContracts::class)
    suspend inline fun <V> withLock(block: T.() -> V): V {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return mutex.withLock {
            value.block()
        }
    }

    @OptIn(ExperimentalContracts::class)
    suspend inline fun <V> withLockLet(block: (T) -> V): V {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return mutex.withLock {
            block(value)
        }
    }
}

fun <T> T.toMutexVar() = MutexVar(this)

suspend fun <T : MutableCollection<V>, V> MutexVar<T>.add(element: V) = withLock { add(element) }

suspend fun <T : MutableCollection<V>, V> MutexVar<T>.remove(element: V) = withLock { remove(element) }

suspend fun <T : MutableCollection<V>, V> MutexVar<T>.clearAndAddAll(other: MutexVar<T>) = withLock {
    clear()
    other.mutex.lock()
    try {
        addAll(other.value)
    } finally {
        other.mutex.unlock()
    }
}
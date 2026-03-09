/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

@Serializable
data class MutexVar<T>(val value: T){
    val mutex = Mutex()

    suspend inline fun <V> withLock(block: T.() -> V) = mutex.withLock {
        block(value)
    }
}

fun <T> T.toMutexVar() = MutexVar(this)

suspend fun <T : MutableCollection<V>, V> MutexVar<T>.add(element: V) = withLock { add(element) }

suspend fun <T : MutableCollection<V>, V> MutexVar<T>.remove(element: V) = withLock { remove(element) }
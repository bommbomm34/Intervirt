/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

@Serializable
data class MutexVar<T>(private var value: T){
    private val mutex = Mutex()

    suspend fun withLock(block: (T) -> Unit) = mutex.withLock {
        block(value)
    }
}

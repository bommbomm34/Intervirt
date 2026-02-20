package io.github.bommbomm34.intervirt.secret

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun catching(block: suspend () -> Unit): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching { block() }.onFailure { if (it is CancellationException) throw it }
}

internal fun ByteArray.zeroize() = fill(0)
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.secret

import io.github.bommbomm34.intervirt.logging.KLogger
import io.github.bommbomm34.intervirt.logging.LogLevel
import io.github.bommbomm34.intervirt.logging.logOnFailure
import uniffi.secret.SecretServiceException
import uniffi.secret.SecretServiceInterface

class SecretService(
    serviceName: String,
    logLevel: LogLevel = LogLevel.ERROR,
    private val service: SecretServiceInterface = uniffi.secret.SecretService(serviceName),
) : AutoCloseable {
    private val logger = KLogger(SecretService::class, logLevel)

    fun setEntry(key: String, value: ByteArray): Result<Unit> = runCatching {
        try {
            service.set(key, value)
            logger.debug { "Set entry $key" }
        } finally {
            value.zeroize()
        }
    }

    fun getEntry(key: String): Result<ByteArray?> = runCatching {
        val value = service.get(key)
        logger.debug { "Retrieved entry $key" }
        value
    }.recoverCatching {
        if (it is SecretServiceException.NoEntry) {
            logger.debug { "Entry $key does not exist" }
            null
        } else {
            logger.error(it) { "Error while retrieving entry $key" }
            throw it
        }
    }

    fun removeEntry(key: String): Result<Unit> = runCatching {
        service.del(key)
        logger.debug { "Deleted entry $key" }
    }.logOnFailure(logger) { "Error while deleting $key" }

    override fun close() {
        if (service is AutoCloseable) service.close()
        logger.debug { "Closed SecretService" }
    }
}

private fun ByteArray.zeroize() = fill(0)
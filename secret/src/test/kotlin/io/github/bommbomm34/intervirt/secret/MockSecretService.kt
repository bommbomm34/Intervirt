/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.secret

import io.github.bommbomm34.intervirt.logging.KLogger
import io.github.bommbomm34.intervirt.logging.LogLevel
import uniffi.secret.SecretServiceException
import uniffi.secret.SecretServiceInterface

class MockSecretService : SecretServiceInterface {
    private val data = mutableMapOf<String, ByteArray>()

    override fun del(key: String) {
        data.remove(key) ?: throwNoEntry()
    }

    override fun get(key: String): ByteArray {
        return data[key] ?: throwNoEntry()
    }

    override fun set(key: String, value: ByteArray) {
        data[key] = value.clone()
    }

    private fun throwNoEntry(): Nothing = throw SecretServiceException.NoEntry()
}

fun getMockSecretService(): SecretService = SecretService(
    serviceName = "mock-secret-service",
    logger = KLogger(SecretService::class, LogLevel.DEBUG),
    service = MockSecretService(),
)

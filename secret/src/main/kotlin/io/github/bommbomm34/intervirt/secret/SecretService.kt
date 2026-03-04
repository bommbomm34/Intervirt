package io.github.bommbomm34.intervirt.secret

import uniffi.secret.SecretService
import uniffi.secret.SecretServiceException

class SecretService(serviceName: String) : AutoCloseable {
    private val service = SecretService(serviceName)

    fun setEntry(key: String, value: ByteArray): Result<Unit> = runCatching {
        service.set(key, value)
    }

    fun getEntry(key: String): Result<ByteArray?> = runCatching {
        service.get(key)
    }.recoverCatching { if (it is SecretServiceException.NoEntry) null else throw it }

    fun removeEntry(key: String): Result<Unit> = runCatching {
        service.del(key)
    }

    override fun close() {
        service.close()
    }
}
package io.github.bommbomm34.intervirt.secret

import uniffi.secret.SecretService

class SecretService(serviceName: String) : AutoCloseable {
    private val service = SecretService(serviceName)

    fun setEntry(key: String, value: ByteArray): Result<Unit> = runCatching {
        service.set(key, value)
    }

    fun getEntry(key: String): Result<ByteArray> = runCatching {
        service.get(key)
    }

    fun removeEntry(key: String): Result<Unit> = runCatching {
        service.del(key)
    }

    override fun close() {
        service.close()
    }
}
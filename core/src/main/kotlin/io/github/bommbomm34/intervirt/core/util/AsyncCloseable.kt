package io.github.bommbomm34.intervirt.core.util

interface AsyncCloseable {
    suspend fun close(): Result<Unit>
}
package io.github.bommbomm34.intervirt.data

import java.io.InputStream
import java.io.OutputStream

data class CommandStream(
    val stdin: InputStream,
    val stderr: OutputStream,
    val stdout: OutputStream,
    private val onClose: () -> Unit
) : AutoCloseable {
    override fun close() = onClose()
}

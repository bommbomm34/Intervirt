package io.github.bommbomm34.intervirt.data

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel

data class QemuMonitorSession(
    val readChannel: ByteReadChannel,
    val writeChannel: ByteWriteChannel
)

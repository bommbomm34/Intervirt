package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

interface ContainerIOClient : AsyncCloseable {

    fun exec(commands: List<String>): Result<Flow<CommandStatus>>

    fun getPath(path: String): Path
}

sealed class ShellControlMessage {
    class ByteData(val bytes: ByteArray) : ShellControlMessage()
    class Kill : ShellControlMessage()
    class End(val statusCode: Int) : ShellControlMessage()
    class Resize(val columns: Int, val rows: Int) : ShellControlMessage()
}
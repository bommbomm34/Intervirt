package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.CommandStatus
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.nio.file.Path

interface ContainerIOClient {
    fun exec(commands: List<String>): Result<Flow<CommandStatus>>
    fun getPath(path: String): Path
    fun close()
}
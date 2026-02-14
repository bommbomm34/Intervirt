package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.CommandStatus
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

interface ContainerIOClient : AutoCloseable {
    fun exec(commands: List<String>): Result<Flow<CommandStatus>>
    fun getPath(path: String): Path
}
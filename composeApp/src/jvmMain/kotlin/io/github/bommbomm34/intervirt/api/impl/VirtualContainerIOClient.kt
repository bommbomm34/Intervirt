package io.github.bommbomm34.intervirt.api.impl

import io.github.bommbomm34.intervirt.addFirst
import io.github.bommbomm34.intervirt.api.ContainerIOClient
import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.api.FileManager
import io.github.bommbomm34.intervirt.data.CommandStatus
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

class VirtualContainerIOClient(
    id: String,
    private val executor: Executor,
    fileManager: FileManager
) : ContainerIOClient {
    private val virtualRoot = fileManager.getFile("virtual/$id").apply { mkdirs() }.toPath()

    override fun exec(commands: List<String>): Result<Flow<CommandStatus>> =
        Result.success(executor.runCommandOnHost(null, commands.addFirst("pkexec")))

    override fun getPath(path: String): Path = virtualRoot.resolve(path.normalize())

    override fun close() {} // Nothing to close

    private fun String.normalize() = if (startsWith("/")) substringAfter("/") else this
}
package io.github.bommbomm34.intervirt.core.api.impl

import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.Executor
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.patch
import io.github.bommbomm34.intervirt.core.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

class VirtualContainerIOClient(
    id: String,
    private val wipeOnClose: Boolean,
    private val executor: Executor,
    fileManager: FileManager,
) : ContainerIOClient {
    private val _virtualRoot = lazy { fileManager.getFile("virtual/$id").apply { mkdirs() }.toPath() }
    private val virtualRoot by _virtualRoot

    override fun exec(commands: List<String>): Result<Flow<CommandStatus>> =
        Result.success(executor.runCommandOnHost(null, commands.patch("sudo", "pkexec")))

    override fun getPath(path: String): Path = virtualRoot.resolve(path.normalize())

    @OptIn(ExperimentalPathApi::class)
    override suspend fun close(): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        if (wipeOnClose && _virtualRoot.isInitialized()) virtualRoot.deleteRecursively()
    }

    private fun String.normalize() = if (startsWith("/")) substringAfter("/") else this
}
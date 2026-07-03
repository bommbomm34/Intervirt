/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.right
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.ContainerIOClientExecInterceptor
import io.github.bommbomm34.intervirt.core.api.DnsResolverInterceptor.command
import io.github.bommbomm34.intervirt.core.api.DnsResolverInterceptor.intercept
import io.github.bommbomm34.intervirt.core.api.Executor
import io.github.bommbomm34.intervirt.core.api.FileManager

import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.util.ext.patch
import io.github.bommbomm34.intervirt.core.util.ext.toJavaPath
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import io.github.vinceglb.filekit.createDirectories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

class VirtualContainerIOClient(
    override val id: String,
    private val wipeOnClose: Boolean,
    private val executor: Executor,
    fileManager: FileManager,
    private val interceptors: List<ContainerIOClientExecInterceptor> = ContainerIOClientExecInterceptor.DEFAULT_VIRTUAL_INTERCEPTORS,
) : ContainerIOClient {
    private val _virtualRoot = lazy { fileManager.getFile("virtual/$id").apply { createDirectories() }.toJavaPath() }
    private val virtualRoot by _virtualRoot

    context(_: Raise<Failure>)
    override fun exec(commands: List<String>): Flow<CommandStatus> {
        val command = commands.getOrNull(0) ?: raise(Failure.IllegalArgument("Expected command"))
        interceptors.singleOrNull { it.command == command }?.let {
            return flow {
                intercept(commands.drop(1))
            }
        }

        return executor.runCommand(null, commands.patch("sudo", "pkexec"))
    }

    override fun getPath(path: String): Path = virtualRoot.resolve(path.normalize())

    @OptIn(ExperimentalPathApi::class)
    context(_: Raise<Failure>)
    override suspend fun close() = withCatchingContext(Dispatchers.IO) {
        if (wipeOnClose && _virtualRoot.isInitialized()) virtualRoot
    }

    private fun String.normalize() = if (startsWith("/")) substringAfter("/") else this
}

/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.impl

import ai.rever.bossterm.compose.PlatformServices
import ai.rever.bossterm.compose.getPlatformServices
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import io.github.bommbomm34.intervirt.core.api.ShellControlMessage
import io.github.bommbomm34.intervirt.core.api.impl.ContainerSshClient
import io.github.bommbomm34.intervirt.core.data.ContainerSshChannel
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.error
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import io.github.bommbomm34.intervirt.logging.debug
import io.github.bommbomm34.intervirt.logging.trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Async
import java.io.ByteArrayOutputStream
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ContainerPlatformServices(
    appEnv: AppEnv,
    private val ioClient: ContainerSshClient,
    private val onError: (Failure) -> Unit,
) : PlatformServices by getPlatformServices(), AsyncCloseable {
    private val logger = appEnv.getLogger(ContainerPlatformServices::class, ioClient.id.value)
    private val handles = mutableListOf<ProcessHandleImpl>()
    private val handlesLock = Mutex()

    private val processService: PlatformServices.ProcessService = ProcessServiceImpl()
    private val fileSystemService: PlatformServices.FileSystemService = FileSystemServiceImpl()

    override fun getProcessService() = processService

    override fun getFileSystemService() = fileSystemService

    context(_: Raise<Failure>)
    override suspend fun close() {
        for (handle in handles) {
            handle.close()
        }
    }

    private inner class ProcessServiceImpl : PlatformServices.ProcessService {
        override suspend fun spawnProcess(config: PlatformServices.ProcessService.ProcessConfig) =
            withContext(Dispatchers.IO) {
                val (command, arguments, environment, _) = config

                logger.debug { "Spawning process with configuration '$config'" }

                val scope = CoroutineScope(Dispatchers.IO)

                recover(
                    block = {
                        val channel = ioClient.pty(
                            command = command,
                            arguments = arguments,
                            environment = environment,
                            // Working directory of 'ProcessConfig' is always the user home directory (not container!!!) in Intervirt.
                            workingDirectory = null, // Defaults to container home
                            scope = scope,
                        )
                        logger.debug { "Spawned process '$command' successfully" }

                        val handle = ProcessHandleImpl(channel, scope)
                        handlesLock.withLock { handles += handle }
                        handle
                    },
                    recover = {
                        logger.error(it) { "Failed spawning process '$command'" }
                        onError(it)
                        null
                    },
                )
            }
    }

    private inner class FileSystemServiceImpl : PlatformServices.FileSystemService {
        override suspend fun fileExists(path: String) = withContext(Dispatchers.IO) {
            ioClient.getPath(path).exists()
        }

        override suspend fun readTextFile(path: String) = withContext(Dispatchers.IO) {
            runCatching { ioClient.getPath(path).readText() }.getOrNull()
        }

        override suspend fun writeTextFile(path: String, content: String) = withContext(Dispatchers.IO) {
            runCatching { ioClient.getPath(path).writeText(path) }.isSuccess
        }

        override fun getUserHomeDirectory() = "/root"

        override fun getTempDirectory() = "/tmp"
    }

    private inner class ProcessHandleImpl(
        private val shell: ContainerSshChannel,
        private val scope: CoroutineScope,
    ) : PlatformServices.ProcessService.ProcessHandle, AsyncCloseable {
        private val outgoing get() = shell.incoming

        private val buffer = ByteArrayOutputStream()

        private val bufferMutex = Mutex()
        private val statusCodeFlow: MutableStateFlow<Int?> = MutableStateFlow(null)
        private var statusCode
            get() = statusCodeFlow.value
            set(value) {
                statusCodeFlow.value = value
            }

        init {
            launchIncomingJob()
        }

        override suspend fun write(data: String) {
            logger.debug { "Sending '$data' to container" }
            writeBytes(data.encodeToByteArray())
        }

        override suspend fun writeBytes(data: ByteArray) {
            logger.trace { "Sending ${data.size} bytes to container" }
            outgoing.send(ShellControlMessage.Bytes(data)).also {
                logger.trace { "Sent ${data.size} bytes to container" }
            }
        }

        override suspend fun read(): String? {
            logger.trace { "Waiting for string from container..." }
            return readBuffer().also {
                logger.trace { "Received string '$it' from container" }
            }
        }

        override fun isAlive() = (!shell.isClosed).also {
            logger.trace {
                if (it) "Container session is alive" else "Container session is closed"
            }
        }

        override suspend fun kill() {
            outgoing.send(ShellControlMessage.Kill).also {
                logger.debug { "Killed container session" }
            }
        }

        override suspend fun waitFor(): Int {
            return statusCodeFlow.last()!!.also {
                logger.debug { "Received status code '$it'" }
            }
        }

        override suspend fun resize(columns: Int, rows: Int) {
            logger.trace { "Resizing to '$columns' columns and '$rows' rows." }
            outgoing.send(ShellControlMessage.Resize(columns, rows)).also {
                logger.debug { "Resized to ${columns}x$rows" }
            }
        }

        override fun getExitCode(): Int? {
            return statusCode.also {
                logger.trace { "Received exit code $it" }
            }
        }

        override fun getPid(): Long? = runBlocking {
            write("echo $$\n")
            read()?.toLong().also {
                logger.trace { "Received PID '$it'" }
            }
        }

        override fun getWorkingDirectory(): String? = runBlocking {
            write("pwd\n")
            read().also {
                logger.trace { "Current working directory: $it" }
            }
        }

        context(_: Raise<Failure>)
        override suspend fun close() = withCatchingContext(Dispatchers.IO) {
            if (statusCode == null) kill()
            val code = waitFor()
            if (code != 0) {
                raise(Failure.ContainerExecution("Shell exited unexpectedly: $code"))
            }
            buffer.close()
            shell.close()
        }

        private fun launchIncomingJob() = scope.launch {
            val incoming = shell.outgoing

            for (message in incoming) {
                logger.trace { "Received message from server: $message" }
                when (message) {
                    is ShellControlMessage.Byte -> {
                        bufferMutex.withLock {
                            buffer.write(message.byte)
                        }
                    }
                    is ShellControlMessage.End -> {
                        logger.debug { "Received end status code '${message.statusCode}'" }
                        statusCode = message.statusCode
                        buffer.close()
                        break
                    }
                }
            }
        }

        private suspend fun readBuffer(): String? = bufferMutex.withLock {
            if (buffer.size() == 0) return null
            val content = buffer.toString()
            buffer.reset()
            content
        }
    }
}

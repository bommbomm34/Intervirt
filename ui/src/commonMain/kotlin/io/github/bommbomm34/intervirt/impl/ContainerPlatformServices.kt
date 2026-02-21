package io.github.bommbomm34.intervirt.impl

import ai.rever.bossterm.compose.PlatformServices
import ai.rever.bossterm.compose.getPlatformServices
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.ShellControlMessage
import io.github.bommbomm34.intervirt.core.api.impl.ContainerSshClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ContainerPlatformServices(
    private val ioClient: ContainerSshClient,
) : PlatformServices by getPlatformServices() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun getProcessService() = object : PlatformServices.ProcessService {
        override suspend fun spawnProcess(config: PlatformServices.ProcessService.ProcessConfig) = withContext(
            Dispatchers.IO,
        ) {
            val pty = ioClient.pty(
                command = config.command,
                arguments = config.arguments,
                environment = config.environment,
                workingDirectory = config.workingDirectory,
            ).getOrNull()

            // TODO: Verify it!!!!
            pty?.let { shell ->
                object : PlatformServices.ProcessService.ProcessHandle {
                    override suspend fun write(data: String) {
                        writeBytes(data.encodeToByteArray())
                    }

                    override suspend fun writeBytes(data: ByteArray) {
                        shell.send(ShellControlMessage.ByteData(data))
                    }

                    override suspend fun read(): String? {
                        return (shell.tryReceive().getOrNull() as? ShellControlMessage.ByteData)?.bytes?.decodeToString()
                    }

                    override fun isAlive() = shell.isClosedForSend && shell.isClosedForReceive

                    override suspend fun kill() = shell.send(ShellControlMessage.Kill())

                    override suspend fun waitFor(): Int {
                        val res = shell
                            .receiveAsFlow()
                            .first { it is ShellControlMessage.End }
                        return (res as ShellControlMessage.End).statusCode
                    }

                    override suspend fun resize(columns: Int, rows: Int) {
                        shell.send(ShellControlMessage.Resize(columns, rows))
                    }

                    override fun getExitCode(): Int? {
                        return (shell.tryReceive().getOrNull() as? ShellControlMessage.End)?.statusCode
                    }

                    override fun getPid(): Long? = runBlocking {
                        write("echo $$\n")
                        read()?.toLong()
                    }

                    override fun getWorkingDirectory(): String? = runBlocking {
                        write("pwd\n")
                        read()
                    }
                }
            }
        }
    }

    override fun getFileSystemService() = object : PlatformServices.FileSystemService {
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
}
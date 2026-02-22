package io.github.bommbomm34.intervirt.core.api.impl

import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.ShellControlMessage
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import io.github.bommbomm34.intervirt.core.withCatchingContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.channel.PtyChannelConfiguration
import org.apache.sshd.sftp.client.fs.SftpFileSystemProvider
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

private const val HOST = "127.0.0.1"
private const val USERNAME = "root"

class ContainerSshClient(
    val port: Int,
    val deviceManager: DeviceManager,
) : ContainerIOClient {
    private val fs: FileSystem = FileSystems.newFileSystem(
        SftpFileSystemProvider.createFileSystemURI(
            HOST, port,
            USERNAME, "",
        ),
        emptyMap<String, Any>(),
    )
    private val sshClient = SshClient.setUpDefaultClient()
    private var session: ClientSession
    private val logger = KotlinLogging.logger { }

    init {
        sshClient.start()
        session = sshClient.connect(USERNAME, HOST, port).verify().session
        session.auth().verify()
    }

    suspend fun pty(
        command: String,
        arguments: List<String>,
        environment: Map<String, String>,
        workingDirectory: String?,
    ) = withCatchingContext(Dispatchers.IO) {
        val totalCommand = listOf(command, *(arguments.toTypedArray()))
        logger.info { "Opening PTY shell '$totalCommand' on container" }
        val sshChannel = session.createShellChannel(null, environment)
        sshChannel.ptyType = "xterm"
        sshChannel.open().verify()
        val channel = Channel<ShellControlMessage>()
        val inputStream = sshChannel.`in`
        val outputStream = sshChannel.out

        launch {
            inputStream.use { _ ->
                while (!sshChannel.isClosed) {
                    val bytes = inputStream.readBytes()
                    channel.send(ShellControlMessage.ByteData(bytes))
                }
            }
            channel.send(ShellControlMessage.End(sshChannel.exitStatus))
            channel.close()
        }

        launch {
            outputStream.use { _ ->
                while (!sshChannel.isClosed) {
                    for (msg in channel) {
                        when (msg) {
                            is ShellControlMessage.ByteData -> {
                                outputStream.write(msg.bytes)
                                outputStream.flush()
                            }

                            is ShellControlMessage.Kill -> {
                                channel.close()
                                inputStream.close()
                                outputStream.close()
                                sshClient.close(true)
                            }

                            is ShellControlMessage.Resize -> {
                                sshChannel.ptyColumns = msg.columns
                                sshChannel.ptyLines = msg.rows
                            }

                            else -> error("Invalid: $msg")
                        }
                    }
                }
            }
        }
        // Switch to working directory
        channel.send(ShellControlMessage.ByteData("cd $workingDirectory\n".encodeToByteArray()))
        // Run command with arguments
        channel.send(ShellControlMessage.ByteData("$totalCommand\n".encodeToByteArray()))

        channel
    }

    override suspend fun close() = withCatchingContext(Dispatchers.IO) {
        session.close()
        sshClient.stop()
        fs.close()
        deviceManager.removePortForwarding(
            externalPort = port,
            protocol = "tcp",
        ).getOrThrow()
    }

    override fun exec(commands: List<String>): Result<Flow<CommandStatus>> = runCatching {
        val command = commands.joinToString(" ")
        logger.info { "Running '$command' on container" }
        val channel = session.createExecChannel(command)
        channel.open().verify()
        flow {
            val reader = channel.`in`.bufferedReader()
            while (!channel.isClosed) {
                val line = reader.readLine() ?: continue
                emit(line.toCommandStatus())
            }
            emit(channel.exitStatus.toCommandStatus())
        }.flowOn(Dispatchers.IO)
    }

    override fun getPath(path: String): Path = fs.getPath(path)
}
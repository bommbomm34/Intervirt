/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.ShellControlMessage
import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.atomic.getValue
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.ContainerSshChannel
import io.github.bommbomm34.intervirt.core.data.ContainerSshConfiguration
import io.github.bommbomm34.intervirt.core.data.DeviceId
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.util.ext.addFirst
import io.github.bommbomm34.intervirt.core.util.ext.exec
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import io.github.bommbomm34.intervirt.logging.debug
import io.github.bommbomm34.intervirt.logging.info
import io.github.bommbomm34.intervirt.logging.warn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.channel.ChannelOutputStream
import org.apache.sshd.sftp.client.fs.SftpFileSystemProvider
import java.io.FileDescriptor
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.Arrays
import java.util.EnumSet

class ContainerSshClient(
    envHolder: AppEnvHolder,
    val conf: ContainerSshConfiguration,
    override val id: DeviceId,
    private val onRemovePortForwarding: suspend context(Raise<Failure>) () -> Unit,
) : ContainerIOClient {
    val appEnv by envHolder
    private val fs: FileSystem = FileSystems.newFileSystem(
        SftpFileSystemProvider.createFileSystemURI(
            /* host = */ conf.host,
            /* port = */ conf.port,
            /* username = */ conf.username,
            /* password = */ conf.password,
        ),
        emptyMap<String, Any>(),
    )
    private val sshClient: SshClient = SshClient.setUpDefaultClient()
    private lateinit var session: ClientSession
    private val logger = appEnv.getLogger(ContainerSshClient::class, id.value)

    context(_: Raise<Failure>)
    suspend fun init() = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Initializing ContainerSshClient" }
        sshClient.start()
        session = sshClient.connect(conf.username, conf.host, conf.port).verify().session
        conf.password?.let(session::addPasswordIdentity)
        session.auth().verify()
        logger.debug { "Initialized ContainerSshClient" }
    }

    context(_: Raise<Failure>)
    suspend fun pty(
        command: String,
        arguments: List<String>,
        environment: Map<String, String>,
        workingDirectory: String?,
        scope: CoroutineScope,
    ): ContainerSshChannel = withCatchingContext(Dispatchers.IO) {
        val totalCommand = "$command ${arguments.joinToString()}"
        logger.info { "Opening PTY shell for command '$totalCommand' on container" }
        val sshChannel = session.createShellChannel(null, environment)
        sshChannel.ptyType = "xterm"
        sshChannel.open().verify()
        val channel = ContainerSshChannel(isClosedSupplier = sshChannel::isClosed)
        val outputStream = requireNotNull(sshChannel.invertedIn) {
            "Expected ChannelShell.invertedIn to be non-null"
        }
        val inputStream = requireNotNull(sshChannel.invertedOut) {
            "Expected ChannelShell.invertedOut to be non-null"
        }.reader()

        scope.launch {
            inputStream.use { _ ->
                while (!sshChannel.isClosed) {
                    val charInt = inputStream.read()
                    if (charInt == -1) break
                    val char = charInt.toChar()

                    if (char.isHighSurrogate()) {
                        // Get low surrogate
                        val lowSurrogateInt = inputStream.read()
                        if (lowSurrogateInt == -1) break
                        val lowSurrogate = lowSurrogateInt.toChar()
                        // Send high and low surrogate
                        val arr = charArrayOf(char, lowSurrogate)
                        channel.outgoing.send(ShellControlMessage.Characters(arr))
                    } else {
                        channel.outgoing.send(ShellControlMessage.Character(charInt))
                    }
                }
            }
            val statusCode = sshChannel.exitStatus ?: 0
            logger.debug { "Sending end with status code '$statusCode'" }
            sshChannel.waitFor(CLOSED_SET, 0L)
            channel.outgoing.send(ShellControlMessage.end(statusCode))
            channel.close()
            sshChannel.close()
        }

        scope.launch {
            outputStream.use { _ ->
                for (msg in channel.incoming) {
                    when (msg) {
                        is ShellControlMessage.Bytes -> {
                            if (!sshChannel.isClosed) {
                                outputStream.write(msg.bytes)
                                outputStream.flush()
                            } else {
                                logger.warn { "Tried to write but the channel is closed: $msg" }
                            }
                        }

                        ShellControlMessage.Kill -> {
                            channel.outgoing.send(ShellControlMessage.end(0))
                            channel.close()
                            sshChannel.close()
                        }

                        is ShellControlMessage.Resize -> {
                            sshChannel.sendWindowChange(msg.columns, msg.rows)
                        }
                    }
                }
            }
        }

        if (workingDirectory != null) {
            logger.debug { "Setting working directory of command '$command'" }
            // Switch to working directory
            channel.incoming.send(ShellControlMessage.Bytes("cd $workingDirectory\n".encodeToByteArray()))
            logger.debug { "Set working directory of command '$command'" }
        }
        if (command != DEFAULT_SHELL) {
            logger.debug { "Running command on PTY shell of '$command'" }
            // Run command with arguments
            channel.incoming.send(ShellControlMessage.Bytes("$totalCommand\n".encodeToByteArray()))
            logger.debug { "Ran command on PTY shell of '$command'" }
        }

        channel
    }

    context(_: Raise<Failure>)
    override suspend fun close() = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Closing ContainerSshClient" }
        session.close()
        sshClient.stop()
        fs.close()
        onRemovePortForwarding()
        logger.debug { "Closed ContainerSshClient" }
    }

    context(_: Raise<Failure>)
    override fun exec(commands: List<String>): Flow<CommandStatus> {
        val command = commands.joinToString(" ")
        logger.info { "Running '$command' on container" }
        return session.exec(command)
    }

    override fun getPath(path: String): Path = fs.getPath(path)

    companion object {
        val CLOSED_SET = setOf(ClientChannelEvent.CLOSED)
        const val DEFAULT_SHELL = "/bin/bash"
    }
}

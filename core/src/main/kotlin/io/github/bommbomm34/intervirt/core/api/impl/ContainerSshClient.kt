package io.github.bommbomm34.intervirt.core.api.impl

import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import io.github.bommbomm34.intervirt.core.withCatchingContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.sftp.client.SftpClientFactory
import org.apache.sshd.sftp.client.fs.SftpFileSystemProvider
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

private const val HOST = "127.0.0.1"
private const val USERNAME = "root"

class ContainerSshClient(override val port: Int) : ContainerIOClient {
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
        val factory = SftpClientFactory.instance()
        sshClient.start()
        session = sshClient.connect(USERNAME, HOST, port).verify().session
        session.auth().verify()
    }


    override suspend fun close() = withCatchingContext(Dispatchers.IO) {
        session.close()
        sshClient.stop()
        fs.close()
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
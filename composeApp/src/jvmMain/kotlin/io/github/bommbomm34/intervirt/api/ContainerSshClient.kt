package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.CommandStream
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.sftp.client.SftpClientFactory
import org.apache.sshd.sftp.client.fs.SftpFileSystemProvider
import java.nio.file.FileSystems

private const val HOST = "127.0.0.1"
private const val USERNAME = "root"

class ContainerSshClient(port: Int) {
    val fs = FileSystems.newFileSystem(
        SftpFileSystemProvider.createFileSystemURI(HOST, port, USERNAME, ""),
        emptyMap<String, Any>()
    )
    private val sshClient = SshClient.setUpDefaultClient()
    private var session: ClientSession

    init {
        val factory = SftpClientFactory.instance()
        sshClient.start()
        session = sshClient.connect(USERNAME, HOST, port).verify().session
        session.auth().verify()
    }

    fun exec(command: String): CommandStream {
        val channel = session.createExecChannel(command)
        channel.open().verify()
        return CommandStream(
            stdin = channel.`in`,
            stdout = channel.out,
            stderr = channel.err
        ) { channel.close() }
    }

    fun close() {
        session.close()
        sshClient.stop()
        fs.close()
    }
}
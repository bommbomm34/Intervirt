package io.github.bommbomm34.intervirt.api

import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ChannelExec
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

    fun exec(command: String): ChannelExec {
        val channel = session.createExecChannel(command)
        channel.isRedirectErrorStream = true
        channel.open().verify()
        return channel
    }

    fun close() {
        session.close()
        sshClient.stop()
        fs.close()
    }
}
package io.github.bommbomm34.intervirt.data

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.Session
import io.github.bommbomm34.intervirt.SSH_PORT
import io.github.bommbomm34.intervirt.jsch
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

class Executor(val fileManagement: FileManagement) {
    val guestSession: Session = jsch.getSession("root", "127.0.0.1", SSH_PORT)

    init {
        guestSession.setConfig("StrictHostKeyChecking", "no");
    }

    fun runCommandOnHost(workingFolder: String, vararg commands: String): Flow<CommandStatus> =
        flow {
            val builder = ProcessBuilder(*commands)
            builder.directory(fileManagement.getFile(workingFolder))
            builder.redirectErrorStream()
            logger.debug { "Running ${commands.joinToString(" ")}" }
            val process = builder.start()
            val reader = process.inputStream.bufferedReader()
            while (process.isAlive) {
                val line = reader.readLine() ?: continue
                logger.debug { "Output: $line" }
                emit(line.toCommandStatus())
            }
            emit(process.exitValue().toCommandStatus())
        }

    fun runCommandOnGuest(command: String): Flow<CommandStatus> = flow {
        logger.debug { "Establishing SSH connection" }
        if (!guestSession.isConnected) guestSession.connect()
        logger.debug { "Establishing channel connection" }
        val channel = guestSession.openChannel("exec") as ChannelExec
        val errorStream = ByteArrayOutputStream()
        channel.setCommand(command)
        channel.setErrStream(errorStream)
        channel.inputStream = null
        channel.connect()

        logger.debug { "Reading output" }
        val inputStream = channel.inputStream
        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { emit(it.toCommandStatus()) }
        }
        emit(channel.exitStatus.toCommandStatus())
        logger.debug { "Disconnecting channel" }
        channel.disconnect()
    }
}

data class CommandStatus(
    val message: String? = null,
    val statusCode: Int? = null
)


fun String.toCommandStatus() = CommandStatus(message = this)
fun Int.toCommandStatus() = CommandStatus(statusCode = this)
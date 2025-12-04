package io.github.bommbomm34.intervirt.data

import com.jcraft.jsch.ChannelExec
import io.github.bommbomm34.intervirt.guestSession
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream

class Executor(val fileManagement: FileManagement) {

    fun runCommandOnHost(workingFolder: String, vararg commands: String): Flow<CommandStatus> =
        flow {
            val builder = ProcessBuilder(*commands)
            builder.directory(fileManagement.getFile(workingFolder))
            builder.redirectErrorStream()
            logger.info { "Running '${commands.joinToString(" ")}' on host" }
            val process = builder.start()
            val reader = process.inputStream.bufferedReader()
            while (process.isAlive) {
                val line = reader.readLine() ?: continue
                emit(line.toCommandStatus())
            }
            emit(process.exitValue().toCommandStatus())
        }

    fun runCommandOnGuest(command: String): Flow<CommandStatus> = flow {
        logger.info { "Running '$command' on guest" }
        if (!guestSession.isConnected) guestSession.connect()
        val channel = guestSession.openChannel("exec") as ChannelExec
        val errorStream = ByteArrayOutputStream()
        channel.setCommand(command)
        channel.setErrStream(errorStream)
        channel.inputStream = null
        channel.connect()

        val inputStream = channel.inputStream
        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { emit(it.toCommandStatus()) }
        }
        emit(channel.exitStatus.toCommandStatus())
        channel.disconnect()
    }
}

data class CommandStatus(
    val message: String? = null,
    val statusCode: Int? = null
)


fun String.toCommandStatus() = CommandStatus(message = this)
fun Int.toCommandStatus() = CommandStatus(statusCode = this)
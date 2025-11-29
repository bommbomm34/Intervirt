package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.api.QEMUSocket
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Executor(val fileManagement: FileManagement) {
    val lock = Mutex()
    var socket: QEMUSocket? = null

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

    fun runCommandOnGuest(command: String): Flow<String> = flow {
        lock.withLock {
            if (socket == null) socket = QEMUSocket.open(5555)
            val reader = socket!!.reader
            val writer = socket!!.writer
            writer.println(command)
            while (true) {
                emit(reader.readLine() ?: break)
            }
        }
    }
}

data class CommandStatus(
    val message: String? = null,
    val statusCode: Int? = null
)


fun String.toCommandStatus() = CommandStatus(message = this)
fun Int.toCommandStatus() = CommandStatus(statusCode = this)
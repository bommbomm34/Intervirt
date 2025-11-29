package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class Executor (val fileManagement: FileManagement) {
    fun runCommandOnHost(workingFolder: String, vararg commands: String): Flow<CommandStatus> = flow {
        val builder = ProcessBuilder(*commands)
        builder.directory(fileManagement.getFile(workingFolder))
        builder.redirectErrorStream()
        logger.debug { "Running ${commands.joinToString(" ")}" }
        val process = builder.start()
        val reader = process.inputStream.bufferedReader()
        while (true) {
            val line = reader.readLine() ?: break
            logger.debug { "Output: $line" }
            emit(line.toCommandStatus())
        }
        emit(process.exitValue().toCommandStatus())
    }
}

data class CommandStatus (
    val message: String? = null,
    val statusCode: Int? = null
)


fun String.toCommandStatus() = CommandStatus(message = this)
fun Int.toCommandStatus() = CommandStatus(statusCode = this)
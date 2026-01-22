package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.client
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.readBytes
import io.ktor.websocket.send
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File

class Executor {
    val logger = KotlinLogging.logger { }
    val sessions = mutableMapOf<String, DefaultClientWebSocketSession>()

    suspend fun getContainerSession(id: String): Result<DefaultClientWebSocketSession> {
        sessions[id]?.let { return Result.success(it) }
        try {
            logger.debug { "Initializing container session with $id" }
            val session = client.webSocketSession(
                method = HttpMethod.Post,
                host = "localhost",
                port = 55436,
                path = "shell?id=$id"
            )
            sessions[id] = session
            return Result.success(session)
        } catch (e: Exception) {
            logger.error { "Container session initialization failed: $e" }
            return Result.failure(e)
        }
    }

    fun runCommandOnHost(workingFolder: File, vararg commands: String): Flow<CommandStatus> =
        flow {
            val builder = ProcessBuilder(*commands)
            builder.directory(workingFolder)
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

    suspend fun writePtyBytesOnContainer(id: String, bytes: ByteArray): Result<Unit> {
        val session = getContainerSession(id).getOrElse {
            return Result.failure(it)
        }
        try {
            session.send(bytes)
            return Result.success(Unit)
        } catch (e: Exception){
            return Result.failure(e)
        }
    }

    suspend fun readPtyBytesOnContainer(id: String): Result<ByteArray> {
        val session = getContainerSession(id).getOrElse {
            return Result.failure(it)
        }
        try {
            val bytes = session.incoming.receive().readBytes()
            return Result.success(bytes)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}

data class CommandStatus(
    val message: String? = null,
    val statusCode: Int? = null
)


fun String.toCommandStatus() = CommandStatus(message = this)
fun Int.toCommandStatus() = CommandStatus(statusCode = this)
suspend fun Flow<CommandStatus>.getTotalCommandStatus(iterate: suspend (CommandStatus) -> Unit = {}): CommandStatus {
    var statusCode: Int? = null
    val totalOutput = StringBuilder()
    collect {
        if (it.statusCode == null) {
            totalOutput.append(it.message)
            iterate(it)
        } else {
            statusCode = it.statusCode
            return@collect
        }
    }
    return CommandStatus(totalOutput.toString(), statusCode)
}
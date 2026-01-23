package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.data.ContainerInputStream
import io.github.bommbomm34.intervirt.data.ContainerOutputStream
import io.github.bommbomm34.intervirt.data.RemoteContainerSession
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File

class Executor {
    private val logger = KotlinLogging.logger { }
    private val sessions = mutableListOf<RemoteContainerSession>()

    suspend fun getContainerSession(id: String): Result<RemoteContainerSession> {
        sessions.firstOrNull { it.id == id }?.let { return Result.success(it) }
        try {
            logger.debug { "Initializing container session with $id" }
            val session = client.webSocketSession(
                method = HttpMethod.Post,
                host = "localhost",
                port = 55436,
                path = "shell?id=$id"
            )
            val flow = session.incoming
                .receiveAsFlow()
                .map { it.readBytes() }
            val remoteSession = RemoteContainerSession(
                id = id,
                websocket = session,
                inputStream = ContainerInputStream(flow),
                outputStream = ContainerOutputStream(
                    executor = this,
                    id = id
                )
            )
            sessions.add(remoteSession)
            return Result.success(remoteSession)
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
        }.websocket
        try {
            session.send(bytes)
            return Result.success(Unit)
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
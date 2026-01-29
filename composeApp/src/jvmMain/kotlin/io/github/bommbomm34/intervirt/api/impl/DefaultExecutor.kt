package io.github.bommbomm34.intervirt.api.impl

import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.data.RemoteContainerSession
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DefaultExecutor(
    private val preferences: Preferences
) : Executor {
    private val logger = KotlinLogging.logger { }
    private val sessions = mutableMapOf<String, RemoteContainerSession>()

    override suspend fun getContainerSession(id: String): Result<RemoteContainerSession> {
        sessions[id]?.let { return Result.success(it) }
        try {
            logger.debug { "Initializing container session with $id" }
            val session = client.webSocketSession(
                method = HttpMethod.Post,
                host = "localhost",
                port = preferences.AGENT_PORT,
                path = "pty?id=$id"
            )
            val remoteContainerSession = RemoteContainerSession(id, session)
            sessions[id] = remoteContainerSession
            return Result.success(remoteContainerSession)
        } catch (e: Exception) {
            logger.error { "Container session initialization failed: $e" }
            return Result.failure(e)
        }
    }

    override fun runCommandOnHost(workingFolder: File?, commands: List<String>): Flow<CommandStatus> =
        flow {
            val builder = ProcessBuilder(commands)
            workingFolder?.let { builder.directory(it) }
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
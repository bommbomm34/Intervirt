package io.github.bommbomm34.intervirt.api.impl

import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.api.RemoteContainerSession
import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.data.AppEnv
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class DefaultExecutor(
    private val appEnv: AppEnv
) : Executor {
    override val logger = KotlinLogging.logger { }
    private val sessions = mutableMapOf<String, WebSocketRemoteContainerSession>()

    override suspend fun getContainerSession(id: String): Result<RemoteContainerSession> {
        val scope = CoroutineScope(Dispatchers.IO)
        sessions[id]?.let { return Result.success(it) }
        try {
            logger.debug { "Initializing container session with $id" }
            val session = client.webSocketSession(
                method = HttpMethod.Post,
                host = "localhost",
                port = appEnv.agentPort,
                path = "pty?id=$id"
            )
            val remoteContainerSession = WebSocketRemoteContainerSession(
                id = id,
                incoming = scope.toByteArrayChannel(session.incoming),
                outgoing = scope.toByteArrayChannel(session.outgoing)
            ) {
                session.close()
            }
            sessions[id] = remoteContainerSession
            return Result.success(remoteContainerSession)
        } catch (e: Exception) {
            logger.error { "Container session initialization failed: $e" }
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
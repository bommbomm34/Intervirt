/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.*
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.exceptions.AgentTimeoutException
import io.github.bommbomm34.intervirt.core.exceptions.IllegalAgentResponseException
import io.github.bommbomm34.intervirt.core.takeWhileInclusive
import io.github.bommbomm34.intervirt.core.util.ext.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

private const val LOG_RAW_JSON = false

class AgentGuestManager(
    appEnv: AppEnv,
    private val client: HttpClient,
) : GuestManager {
    private val logger = appEnv.getLogger(AgentGuestManager::class)
    private var session: DefaultClientWebSocketSession? = null
    private var listenJob: Job? = null
    private val requests = ConcurrentHashMap<String, MutableSharedFlow<ResponseBody>>()
    private val agentPort = appEnv.AGENT_PORT
    private val timeout = appEnv.AGENT_WEBSOCKET_TIMEOUT.milliseconds
    private val host = appEnv.AGENT_HOST

    override suspend fun addContainer(
        id: String,
        ipv4: String,
        ipv6: String,
        mac: String,
        internet: Boolean,
        image: String,
    ): Flow<ResultProgress<Unit>> = flowSend(RequestBody.AddContainer(id, ipv4, ipv6, mac, internet, image))

    override suspend fun removeContainer(id: String): Result<Unit> = justSend(RequestBody.RemoveContainer(id))

    override suspend fun setIpv4(id: String, newIP: String): Result<Unit> =
        justSend(RequestBody.IDWithNewIpv4(id, newIP))

    override suspend fun setIpv6(id: String, newIP: String): Result<Unit> =
        justSend(RequestBody.IDWithNewIpv6(id, newIP))

    override suspend fun connect(container: String, network: String): Result<Unit> {
        logger.debug { "Connecting $container to $network" }
        return justSend(RequestBody.Connect(container, network))
    }

    override suspend fun disconnect(container: String, network: String): Result<Unit> {
        logger.debug { "Disconnecting $container from $network" }
        return justSend(RequestBody.Disconnect(container, network))
    }

    override suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit> =
        justSend(RequestBody.SetInternetAccess(id, enabled))

    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String,
    ): Result<Unit> =
        justSend(RequestBody.AddPortForwarding(id, internalPort, externalPort, protocol))

    override suspend fun removePortForwarding(id: String, externalPort: Int, protocol: String): Result<Unit> =
        justSend(RequestBody.RemovePortForwarding(externalPort, protocol))

    override suspend fun startContainer(id: String): Result<Unit> = justSend(RequestBody.StartContainer(id))

    override suspend fun stopContainer(id: String): Result<Unit> = justSend(RequestBody.StopContainer(id))

    override fun wipe(): Flow<ResultProgress<Unit>> {
        logger.debug { "Wiping guest" }
        return flowSend("wipe".commandBody())
    }

    override fun update(): Flow<ResultProgress<Unit>> {
        logger.debug { "Updating guest" }
        return flowSend("update".commandBody())
    }

    override suspend fun shutdown(): Result<Unit> {
        logger.debug { "Shutting down guest" }
        return justSend("shutdown".commandBody())
    }

    override suspend fun reboot(): Result<Unit> {
        logger.debug { "Rebooting guest" }
        return justSend("reboot".commandBody())
    }

    override suspend fun getVersion(): Result<String> {
        logger.debug { "Retrieving version of guest" }
        return firstSend<ResponseBody.Info>("version".commandBody()).map { it.version }
    }

    override suspend fun getContainers(): Result<List<ContainerInfo>> {
        logger.debug { "Retrieving containers of guest" }
        return firstSend<ResponseBody.ContainerList>("containers".commandBody()).map { it.containers }
    }

    override suspend fun addNetwork(name: String): Result<Unit> {
        logger.debug { "Adding network $name" }
        return justSend(RequestBody.AddNetwork(name))
    }

    override suspend fun removeNetwork(name: String): Result<Unit> {
        logger.debug { "Removing network $name" }
        return justSend(RequestBody.RemoveNetwork(name))
    }

    override suspend fun getNetworks(): Result<Map<String, Network>> {
        logger.debug { "Retrieving networks" }
        return firstSend<ResponseBody.NetworkList>("networks".commandBody()).map { it.networks }
    }

    private suspend fun justSend(body: RequestBody): Result<Unit> {
        val response = send<ResponseBody.General>(body)
        return response.map {
            it
                .catchTimeout { throw AgentTimeoutException(body.uuid) }
                .firstOrNull()?.exception()?.result() ?: Result.success(Unit)
        }
    }

    private suspend inline fun <reified T : ResponseBody> firstSend(body: RequestBody): Result<T> {
        val response = send<T>(body)
        return response.mapCatching {
            it
                .catchTimeout { throw AgentTimeoutException(body.uuid) }
                .first()
        }
    }

    private fun flowSend(body: RequestBody): Flow<ResultProgress<Unit>> = flow {
        var failed = false
        send<ResponseBody.General>(body)
            .onSuccess { flow ->
                flow
                    .catchTimeout {
                        this@flow.emit(ResultProgress.failure(AgentTimeoutException(body.uuid)))
                    }
                    .collect {
                        if (!it.success) {
                            failed = true
                            val exception = it.exception()!!
                            logger.error(exception) { "Request failed: $body" }
                            emit(ResultProgress.failure(exception))
                        } else {
                            emit(ResultProgress.proceed(it.progress ?: 0f, it.output))
                        }
                    }
            }
            .onFailure {
                failed = true
                emit(ResultProgress.failure(it))
            }
        if (!failed) emit(ResultProgress.success(Unit))
    }

    @OptIn(FlowPreview::class)
    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <reified T : ResponseBody> send(body: RequestBody): Result<Flow<T>> {
        logger.debug { "Sending request $body with UUID ${body.uuid}" }
        return listen().map {
            requests[body.uuid] = MutableSharedFlow()
            session!!.sendSerialized(body)
            requests[body.uuid]!!
                .map {
                    if (it.success) {
                        it as? T ?: throw IllegalAgentResponseException(
                            "Expected ${T::class.simpleName} as response, but got ${it::class.simpleName}: $it",
                            body.uuid,
                        )
                    } else throw (it as ResponseBody.General).exception()!!
                }
                .takeWhileInclusive { !it.end }
                .onCompletion { throwable ->
                    throwable?.let {
                        if (!it.isMuted()) logger.error(it) { "Failed request: ${body.uuid}" }
                    } ?: logger.debug { "Completed request ${body.uuid}" }
                    requests.remove(body.uuid)
                }
//                .timeout(timeout) // TODO: Fix timeout bug
        }
    }

    private suspend fun listen(): Result<Unit> {
        if (session == null) {
            val result = runSuspendingCatching {
                session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = host,
                    port = agentPort,
                    path = "containerManagement",
                )
                session!!
            }
            result.onSuccess { session ->
                listenJob = CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        try {
                            val response = session.receiveLogging()

                            requests[response.refID]?.let {
                                it.emit(response)
                                logger.debug { "Received response successfully: $response" }
                            } ?: logger.error { "Received response without corresponding request: $response" }
                        } catch (e: WebsocketDeserializeException) {
                            if (e.frame is Frame.Close) break else throw e
                        }
                    }
                }
            }
            return result.map { }
        }

        return Result.success(Unit)
    }

    override suspend fun close() = withCatchingContext(Dispatchers.IO) {
        listenJob?.cancel()
        session?.close()
        Unit
    }

    private fun Throwable.isMuted(): Boolean = setOf(
        this is TimeoutCancellationException,
        this::class.qualifiedName == "kotlinx.coroutines.flow.internal.AbortFlowException",
    ).any { it }

    private suspend fun ClientWebSocketSession.receiveLogging(): ResponseBody {
        val text = when (val frame = incoming.receive()) {
            is Frame.Text -> frame.readText()
            is Frame.Binary -> throw WebsocketDeserializeException("Frame should be Frame.text", frame = frame)
            is Frame.Close -> throw WebsocketDeserializeException("Session is closed", frame = frame)
            else -> throw WebsocketDeserializeException("Unexpected frame type: $frame", frame = frame)
        }

        if (LOG_RAW_JSON) logger.debug { "Received JSON: $text" }
        return defaultJson.decodeFromString(text)
    }
}

/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import arrow.atomic.Atomic
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import inet.ipaddr.AddressStringException
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.AgentInfo
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.*
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.error
import io.github.bommbomm34.intervirt.core.exceptions.AgentTimeoutException
import io.github.bommbomm34.intervirt.core.exceptions.IllegalAgentResponseException
import io.github.bommbomm34.intervirt.core.takeWhileInclusive
import io.github.bommbomm34.intervirt.core.util.atomic
import io.github.bommbomm34.intervirt.core.util.ext.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

private const val LOG_RAW_JSON = false

class AgentGuestManager(
    appEnv: AppEnv,
    private val client: HttpClient,
) : GuestManager {
    private val logger = appEnv.getLogger(AgentGuestManager::class)
    private var session: DefaultClientWebSocketSession? = null
    private var listenJob: Job? = null
    private val requests = ConcurrentHashMap<String, MutableSharedFlow<AppResult<ResponseBody>>>()
    private val agentPort = appEnv.AGENT_PORT
    private val timeout = appEnv.AGENT_WEBSOCKET_TIMEOUT.milliseconds
    private val host = appEnv.AGENT_HOST
    private var agentInfo: Atomic<AgentInfo?> = Atomic(null)

    override suspend fun addContainer(
        id: String,
        ipv4: String,
        ipv6: String,
        mac: String,
        internet: Boolean,
        image: String,
    ): Flow<ResultProgress<Unit>> = flowSend(RequestBody.AddContainer(id, ipv4, ipv6, mac, internet, image))

    override suspend fun removeContainer(id: String): AppResult<Unit> = justSend(RequestBody.RemoveContainer(id))

    override suspend fun setIpv4(id: String, newIP: String): AppResult<Unit> =
        justSend(RequestBody.IDWithNewIpv4(id, newIP))

    override suspend fun setIpv6(id: String, newIP: String): AppResult<Unit> =
        justSend(RequestBody.IDWithNewIpv6(id, newIP))

    override suspend fun connect(container: String, network: String): AppResult<Unit> {
        logger.debug { "Connecting $container to $network" }
        return justSend(RequestBody.Connect(container, network))
    }

    override suspend fun disconnect(container: String, network: String): AppResult<Unit> {
        logger.debug { "Disconnecting $container from $network" }
        return justSend(RequestBody.Disconnect(container, network))
    }

    override suspend fun setInternetAccess(id: String, enabled: Boolean): AppResult<Unit> =
        justSend(RequestBody.SetInternetAccess(id, enabled))

    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String,
    ): AppResult<Unit> =
        justSend(RequestBody.AddPortForwarding(id, internalPort, externalPort, protocol))

    override suspend fun removePortForwarding(id: String, externalPort: Int, protocol: String): AppResult<Unit> =
        justSend(RequestBody.RemovePortForwarding(externalPort, protocol))

    override suspend fun startContainer(id: String): AppResult<Unit> = justSend(RequestBody.StartContainer(id))

    override suspend fun stopContainer(id: String): AppResult<Unit> = justSend(RequestBody.StopContainer(id))

    override fun wipe(): Flow<ResultProgress<Unit>> {
        logger.debug { "Wiping guest" }
        return flowSend("wipe".commandBody())
    }

    override fun update(): Flow<ResultProgress<Unit>> {
        logger.debug { "Updating guest" }
        return flowSend("update".commandBody())
    }

    override suspend fun shutdown(): AppResult<Unit> {
        logger.debug { "Shutting down guest" }
        return justSend("shutdown".commandBody())
    }

    override suspend fun reboot(): AppResult<Unit> {
        logger.debug { "Rebooting guest" }
        return justSend("reboot".commandBody())
    }

    override suspend fun getInfo(): AppResult<AgentInfo> {
        logger.debug { "Retrieving version of guest" }
        agentInfo.get()?.let { return it.right() }
        return firstSend<ResponseBody.Info>("version".commandBody()).flatMap {
            try {
                AgentInfo(
                    version = it.version,
                    ipv4Subnet = IPAddressString(it.ipv4Subnet).getAddress(),
                    ipv6Subnet = IPAddressString(it.ipv6Subnet).getAddress(),
                ).also { info -> agentInfo.compareAndSet(null, info) }.right()
            } catch (e: AddressStringException) {
                Failure.IllegalAgentResponse("Invalid Info response (${e.message}): $it").left()
            }
        }
    }

    override suspend fun getContainers(): AppResult<List<ContainerInfo>> {
        logger.debug { "Retrieving containers of guest" }
        return firstSend<ResponseBody.ContainerList>("containers".commandBody()).map { it.containers }
    }

    override suspend fun addNetwork(name: String): AppResult<Unit> {
        logger.debug { "Adding network $name" }
        return justSend(RequestBody.AddNetwork(name))
    }

    override suspend fun removeNetwork(name: String): AppResult<Unit> {
        logger.debug { "Removing network $name" }
        return justSend(RequestBody.RemoveNetwork(name))
    }

    override suspend fun getNetworks(): AppResult<Map<String, Network>> {
        logger.debug { "Retrieving networks" }
        return firstSend<ResponseBody.NetworkList>("networks".commandBody()).map { it.networks }
    }

    private suspend fun justSend(body: RequestBody): AppResult<Unit> {
        val response = send<ResponseBody.General>(body)
        return response.map {
            it
                .catchTimeout { throw AgentTimeoutException(body.uuid) }
                .firstOrNull()?.failure()?.left() ?: Unit.right()
        }
    }

    private suspend inline fun <reified T : ResponseBody> firstSend(body: RequestBody): AppResult<T> {
        val response = send<T>(body)
        return response.flatMap {
            var timeoutExceeded = false
            val result = it
                .catchTimeout { timeoutExceeded = true }
                .firstOrNull()

            // TODO: Check
            if (timeoutExceeded) Failure.AgentTimeout(body.uuid).left() else result!!
        }
    }

    private fun flowSend(body: RequestBody): Flow<ResultProgress<Unit>> = flow {
        var failed = false
        send<ResponseBody.General>(body)
            .onRight { flow ->
                flow
                    .catchTimeout {
                        this@flow.emit(ResultProgress.failure(Failure.AgentTimeout(body.uuid)))
                    }
                    .collect { result ->
                        val failure = result.fold(
                            ifLeft = { it },
                            ifRight = {
                                if (!it.success) it.failure()!! else null
                            }
                        )
                        if (failure != null) {
                            failed = true
                            logger.error(failure) { "Request failed: $body" }
                            emit(ResultProgress.failure(failure))
                        } else {
                            val responseBody = (result as Either.Right<ResponseBody.General>).value
                            emit(ResultProgress.proceed(responseBody.progress ?: 0f, responseBody.output))
                        }
                    }
            }
            .onLeft {
                failed = true
                emit(ResultProgress.failure(it))
            }
        if (!failed) emit(ResultProgress.success(Unit))
    }

    @OptIn(FlowPreview::class)
    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <reified T : ResponseBody> send(body: RequestBody): AppResult<Flow<AppResult<T>>> {
        logger.debug { "Sending request $body with UUID ${body.uuid}" }
        return listen().map {
            requests[body.uuid] = MutableSharedFlow()
            session!!.sendSerialized(body)
            requests[body.uuid]!!
                .map { result ->
                    result.fold(
                        ifLeft = { it.left() },
                        ifRight = {
                            if (it.success) {
                                (it as? T)?.right() ?: Failure.IllegalAgentResponse(
                                    "Expected ${T::class.simpleName} as response, but got ${it::class.simpleName}: $it",
                                    body.uuid,
                                ).left()
                            } else (it as ResponseBody.General).failure()!!.left()
                        }
                    )
                }
                .takeWhileInclusive { !((it as? Either.Right<T>)?.value?.end ?: true) }
                .onCompletion { throwable ->
                    throwable?.let {
                        if (!it.isMuted()) logger.error(it) { "Failed request: ${body.uuid}" }
                    } ?: logger.debug { "Completed request ${body.uuid}" }
                    requests.remove(body.uuid)
                }
//                .timeout(timeout) // TODO: Fix timeout bug
        }
    }

    private suspend fun listen(): AppResult<Unit> {
        if (session == null) {
            val result = Failure.catch {
                session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = host,
                    port = agentPort,
                    path = "containerManagement",
                )
                session!!
            }
            result.onRight { session ->
                listenJob = CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        try {
                            val response = session.receiveLogging()

                            requests[response.refID]?.let {
                                it.emit(response.right())
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

        return Unit.right()
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

    private fun AppResult<ResponseBody.General>.failure(): Failure? = fold(
        ifLeft = { it },
        ifRight = { it.failure() }
    )
}

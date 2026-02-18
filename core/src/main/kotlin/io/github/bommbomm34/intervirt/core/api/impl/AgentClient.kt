package io.github.bommbomm34.intervirt.core.api.impl

import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.RequestBody
import io.github.bommbomm34.intervirt.core.data.agent.ResponseBody
import io.github.bommbomm34.intervirt.core.data.agent.commandBody
import io.github.bommbomm34.intervirt.core.result
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

class AgentClient(
    appEnv: AppEnv,
    private val client: HttpClient,
) : GuestManager {
    private val logger = KotlinLogging.logger { }
    private var session: DefaultClientWebSocketSession? = null
    private var listenJob: Job? = null
    private val requests = ConcurrentHashMap<String, MutableSharedFlow<ResponseBody>>()
    private val agentPort = appEnv.agentPort
    private val timeout = appEnv.agentWebSocketTimeout.milliseconds

    override suspend fun addContainer(
        id: String,
        initialIpv4: String,
        initialIpv6: String,
        mac: String,
        internet: Boolean,
        image: String,
    ): Result<Unit> = justSend(RequestBody.AddContainer(id, initialIpv4, initialIpv6, mac, internet, image))

    override suspend fun removeContainer(id: String): Result<Unit> = justSend(RequestBody.RemoveContainer(id))

    override suspend fun setIpv4(id: String, newIP: String): Result<Unit> =
        justSend(RequestBody.IDWithNewIpv4(id, newIP))

    override suspend fun setIpv6(id: String, newIP: String): Result<Unit> =
        justSend(RequestBody.IDWithNewIpv6(id, newIP))

    override suspend fun connect(id1: String, id2: String): Result<Unit> = justSend(RequestBody.Connect(id1, id2))

    override suspend fun disconnect(id1: String, id2: String): Result<Unit> =
        justSend(RequestBody.Disconnect(id1, id2))

    override suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit> =
        justSend(RequestBody.SetInternetAccess(id, enabled))

    override suspend fun addPortForwarding(
        id: String,
        internalPort: Int,
        externalPort: Int,
        protocol: String,
    ): Result<Unit> =
        justSend(RequestBody.AddPortForwarding(id, internalPort, externalPort, protocol))

    override suspend fun removePortForwarding(externalPort: Int, protocol: String): Result<Unit> =
        justSend(RequestBody.RemovePortForwarding(externalPort, protocol))

    override suspend fun startContainer(id: String): Result<Unit> = justSend(RequestBody.StartContainer(id))

    override suspend fun stopContainer(id: String): Result<Unit> = justSend(RequestBody.StopContainer(id))

    override fun wipe(): Flow<ResultProgress<Unit>> = flowSend("wipe".commandBody())

    override fun update(): Flow<ResultProgress<Unit>> = flowSend("update".commandBody())

    override suspend fun shutdown(): Result<Unit> = justSend("shutdown".commandBody())

    override suspend fun reboot(): Result<Unit> = justSend("reboot".commandBody())

    override suspend fun getVersion(): Result<String> {
        val response = send<ResponseBody.Version>("version".commandBody())
        response
            .onSuccess {
                return Result.success(it.firstOrNull()!!.version)
            }
            .onFailure {
                return Result.failure(it)
            }
        return Result.failure(UnknownError())
    }

    private suspend fun justSend(body: RequestBody): Result<Unit> {
        val response = send<ResponseBody.General>(body)
        response
            .onSuccess {
                return it.firstOrNull()?.exception()?.result() ?: Result.success(Unit)
            }
            .onFailure {
                return Result.failure(it)
            }
        return Result.failure(UnknownError())
    }

    private fun flowSend(body: RequestBody): Flow<ResultProgress<Unit>> = flow {
        var failed = false
        send<ResponseBody.General>(body)
            .onSuccess { flow ->
                flow.collect {
                    if (it.code != 0) {
                        failed = true
                        emit(ResultProgress.failure(it.exception()!!))
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
    private suspend fun <T : ResponseBody> send(body: RequestBody): Result<Flow<T>> {
        logger.debug { "Checking connection with agent" }
        listen().fold(
            onSuccess = {
                requests[body.uuid] = MutableSharedFlow()
                session!!.sendSerialized(body)
                return Result.success(
                    requests[body.uuid]!!
                        .onCompletion {
                            requests.remove(body.uuid)
                        }
                        .map { it as T }
                        .timeout(timeout)
                        .catch { exception ->
                            if (exception is TimeoutCancellationException) {
                                // TODO: Handle timeout situations also for ResponseBody.Version
                                emit(
                                    ResponseBody.General(
                                        refID = body.uuid,
                                        code = 100,
                                    ) as T,
                                )
                            } else throw exception
                        },
                )
            },
            onFailure = { return Result.failure(it) },
        )
    }

    private suspend fun listen(): Result<Unit> {
        session?.let { ws ->
            val result = runSuspendingCatching {
                session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = "localhost",
                    port = agentPort,
                    path = "containerManagement",
                )
            }
            result.onSuccess {
                listenJob = CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        try {
                            val response = ws.receiveDeserialized<ResponseBody>()
                            requests[response.refID]?.emit(response)
                                ?: logger.error { "Received response without corresponding request: $response" }
                        } catch (e: WebsocketDeserializeException) {
                            if (e.frame is Frame.Close) break else throw e
                        }
                    }
                }
            }
            return result
        }

        return Result.success(Unit)
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        runSuspendingCatching {
            listenJob?.cancel()
            session?.close()
            Unit
        }
    }
}
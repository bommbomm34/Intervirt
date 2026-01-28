package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.data.*
import io.github.bommbomm34.intervirt.result
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class AgentClient(
    preferences: Preferences
) {
    private val logger = KotlinLogging.logger { }
    private lateinit var session: DefaultClientWebSocketSession
    private val requests = ConcurrentHashMap<String, MutableSharedFlow<ResponseBody>>()
    private val agentPort = preferences.AGENT_PORT

    suspend fun addContainer(
        id: String,
        initialIpv4: String,
        initialIpv6: String,
        mac: String,
        internet: Boolean,
        image: String
    ): Result<Unit> = justSend(RequestBody.AddContainer(id, initialIpv4, initialIpv6, mac, internet, image))

    suspend fun removeContainer(id: String): Result<Unit> = justSend(RequestBody.RemoveContainer(id))

    suspend fun downloadFile(id: String, path: String, fileManager: FileManager): Result<File> {
        val fileExtension = path.substringAfterLast(".", "")
        return fileManager.downloadFile(
            "http://localhost:$agentPort/file?id=$id&$path",
            "file-$id-${System.currentTimeMillis()}${if (fileExtension.isBlank()) "" else ".$fileExtension"}"
        ).first { it.result != null }.result!!
    }

    fun uploadFile(id: String, file: File, path: String, fileManager: FileManager): Flow<ResultProgress<Unit>> {
        return fileManager.uploadFile(
            "http://localhost:$agentPort/file?id=$id&path=$path",
            file
        )
    }

    suspend fun setIpv4(id: String, newIP: String): Result<Unit> =
        justSend(RequestBody.IDWithNewIpv4(id, newIP))

    suspend fun setIpv6(id: String, newIP: String): Result<Unit> =
        justSend(RequestBody.IDWithNewIpv6(id, newIP))

    suspend fun connect(id1: String, id2: String): Result<Unit> = justSend(RequestBody.Connect(id1, id2))

    suspend fun disconnect(id1: String, id2: String): Result<Unit> =
        justSend(RequestBody.Disconnect(id1, id2))

    suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit> =
        justSend(RequestBody.SetInternetAccess(id, enabled))

    suspend fun addPortForwarding(id: String, internalPort: Int, externalPort: Int, protocol: String): Result<Unit> =
        justSend(RequestBody.AddPortForwarding(id, internalPort, externalPort, protocol))

    suspend fun removePortForwarding(externalPort: Int, protocol: String): Result<Unit> =
        justSend(RequestBody.RemovePortForwarding(externalPort, protocol))

    fun wipe(): Flow<ResultProgress<Unit>> = flowSend("wipe".commandBody())

    fun update(): Flow<ResultProgress<Unit>> = flowSend("update".commandBody())

    suspend fun shutdown(): Result<Unit> = justSend("shutdown".commandBody())

    suspend fun reboot(): Result<Unit> = justSend("reboot".commandBody())

    suspend fun getVersion(): Result<String> {
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

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : ResponseBody> send(body: RequestBody): Result<Flow<T>> {
        logger.debug { "Checking connection with agent" }
        listen().fold(
            onSuccess = {
                requests[body.uuid] = MutableSharedFlow()
                session.sendSerialized(body)
                return Result.success(requests[body.uuid]!!.onCompletion {
                    requests.remove(body.uuid)
                }.map { it as T })
            },
            onFailure = { return Result.failure(it) }
        )
    }

    private suspend fun listen(): Result<Unit> {
        if (!this::session.isInitialized) {
            val result = runCatching {
                session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = "localhost",
                    port = agentPort,
                    path = "containerManagement"
                )
            }
            result.onSuccess {
                CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        try {
                            val response = session.receiveDeserialized<ResponseBody>()
                            requests[response.refID]!!.emit(response)
                        } catch (e: WebsocketDeserializeException) {
                            if (e.frame is Frame.Close) break
                        }
                    }
                }
            }
            return result
        }
        return Result.success(Unit)
    }
}
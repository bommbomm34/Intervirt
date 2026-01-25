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

class AgentClient {
    private val logger = KotlinLogging.logger { }
    private lateinit var session: DefaultClientWebSocketSession
    private val requests = ConcurrentHashMap<String, MutableSharedFlow<ResponseBody>>()

    suspend fun listen(): Result<Unit> {
        if (!this::session.isInitialized) {
            val result = runCatching {
                session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = "localhost",
                    port = 55436,
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

    suspend fun addContainer(
        id: String,
        initialIpv4: String,
        initialIpv6: String,
        mac: String,
        internet: Boolean,
        image: String
    ): Result<Unit> = justSend(RequestBody.AddContainer(id, initialIpv4, initialIpv6, mac, internet, image))

    suspend fun removeContainer(id: String): Result<Unit> = justSend(RequestBody.RemoveContainer(id))

    suspend fun getDisk(id: String, fileManager: FileManager): Result<File> {
        return fileManager.downloadFile(
            "http://localhost:55436/disk?id=$id",
            "disk-$id-${System.currentTimeMillis()}.tar.gz"
        ).first { it.result != null }.result!!
    }

    fun uploadDisk(id: String, file: File, fileManager: FileManager): Flow<ResultProgress<Unit>> {
        return fileManager.uploadFile(
            "http://localhost:55436/disk?id=$id",
            file
        )
    }

    suspend fun downloadFile(id: String, path: String, fileManager: FileManager): Result<File> {
        val fileExtension = path.substringAfterLast(".", "")
        return fileManager.downloadFile(
            "http://localhost:55436/file?id=$id&$path",
            "file-$id-${System.currentTimeMillis()}${if (fileExtension.isBlank()) "" else ".$fileExtension"}"
        ).first { it.result != null }.result!!
    }

    fun uploadFile(id: String, file: File, path: String, fileManager: FileManager): Flow<ResultProgress<Unit>> {
        return fileManager.uploadFile(
            "http://localhost:55436/file?id=$id&path=$path",
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

    suspend fun addPortForwarding(id: String, internalPort: Int, externalPort: Int): Result<Unit> =
        justSend(RequestBody.AddPortForwarding(id, internalPort, externalPort))

    suspend fun removePortForwarding(externalPort: Int): Result<Unit> =
        justSend(RequestBody.RemovePortForwarding(externalPort))

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

    fun runCommand(id: String, command: String): Flow<ResultProgress<Unit>> =
        flowSend(RequestBody.RunCommand(id, command))

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
}
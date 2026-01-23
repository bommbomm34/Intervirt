package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.data.*
import io.github.bommbomm34.intervirt.result
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.io.File

class AgentClient {
    private val logger = KotlinLogging.logger { }
    private lateinit var session: DefaultClientWebSocketSession
    private var sessionLock = Mutex()

    suspend fun initConnection(): Result<Unit> {
        try {
            if (!this::session.isInitialized) {
                session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = "localhost",
                    port = 55436,
                    path = "containerManagement"
                )
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
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
        val response = send<VersionResponseBody>("version".commandBody())
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
        val response = send<ResponseBody>(body)
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
        send<ResponseBody>(body)
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

    private suspend inline fun <reified T> send(body: RequestBody): Result<Flow<T>> {
        val flow = flow {
            val requestMessage = Json.encodeToString(body)
            logger.debug { "CLIENT: $requestMessage" }
            session.send(Frame.Text(requestMessage))
            while (true) {
                val responseMessage = (session.incoming.receive() as Frame.Text).readText()
                val data = Json.decodeFromString<T>(responseMessage)
                logger.debug { "AGENT: $responseMessage" }
                emit(data)
                if (!(data is ResponseBody && data.code == -1)) {
                    logger.debug { "AGENT END" }
                    break
                }
            }
        }
        sessionLock.withLock {
            logger.debug { "Checking connection with agent" }
            initConnection().fold(
                onSuccess = { return Result.success(flow) },
                onFailure = { return Result.failure(it) }
            )
        }
    }
}
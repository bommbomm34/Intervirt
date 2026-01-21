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

object AgentClient {
    val logger = KotlinLogging.logger {  }
    var session: DefaultClientWebSocketSession? = null
    var sessionLock = Mutex()

    suspend fun initConnection(): Result<Unit> {
        try {
            session = client.webSocketSession(
                method = HttpMethod.Get,
                host = "localhost",
                port = 55436,
                path = "containerManagement"
            )
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun addContainer(
        id: String,
        initialIPv4: String,
        initialIPv6: String,
        mac: String,
        internet: Boolean,
        image: String
    ): Result<Unit> = justSend(RequestBody.AddContainer(id, initialIPv4, initialIPv6, mac, internet, image))

    suspend fun removeContainer(id: String): Result<Unit> = justSend(id.idBody("removeContainer"))

    suspend fun getDisk(id: String): Result<File> {
        return FileManager.downloadFile(
            "http://localhost:55436/disk?id=$id",
            "disk-$id-${System.currentTimeMillis()}.tar.gz"
        ).first { it.result != null }.result!!
    }

    fun uploadDisk(id: String, file: File): Flow<ResultProgress<Unit>> {
        return FileManager.uploadFile(
            "http://localhost:55436/disk?id=$id",
            file
        )
    }

    suspend fun downloadFile(id: String, path: String): Result<File> {
        val fileExtension = path.substringAfterLast(".", "")
        return FileManager.downloadFile(
            "http://localhost:55436/file?id=$id&$path",
            "file-$id-${System.currentTimeMillis()}${if (fileExtension.isBlank()) "" else ".$fileExtension"}"
        ).first { it.result != null }.result!!
    }

    fun uploadFile(id: String, file: File, path: String): Flow<ResultProgress<Unit>> {
        return FileManager.uploadFile(
            "http://localhost:55436/file?id=$id&path=$path",
            file
        )
    }

    suspend fun setIPv4(id: String, newIP: String): Result<Unit> =
        justSend(RequestBody.IDWithNewIP(id, newIP, "setIPv4"))

    suspend fun setIPv6(id: String, newIP: String): Result<Unit> =
        justSend(RequestBody.IDWithNewIP(id, newIP, "setIPv6"))

    suspend fun connect(id1: String, id2: String): Result<Unit> = justSend(RequestBody.Connect(id1, id2, "connect"))

    suspend fun disconnect(id1: String, id2: String): Result<Unit> =
        justSend(RequestBody.Connect(id1, id2, "disconnect"))

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

    fun runCommand(id: String, shellCommand: String, stateless: Boolean = true): Flow<ResultProgress<Unit>> =
        flowSend(RequestBody.RunCommand(id, shellCommand, stateless))

    suspend fun justSend(body: RequestBody): Result<Unit> {
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

    fun flowSend(body: RequestBody): Flow<ResultProgress<Unit>> = flow {
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

    suspend inline fun <reified T> send(body: RequestBody): Result<Flow<T>> {
        val flow = flow<T> {
            val requestMessage = Json.encodeToString(body)
            logger.debug { "CLIENT: $requestMessage" }
            session!!.send(Frame.Text(requestMessage))
            while (true) {
                val responseMessage = (session!!.incoming.receive() as Frame.Text).readText()
                logger.debug { "AGENT: $responseMessage" }
                if (responseMessage == "END") {
                    logger.debug { "AGENT END" }
                    break
                } else emit(Json.decodeFromString(responseMessage))
            }
        }
        sessionLock.withLock {
            logger.debug { "Checking connection with agent" }
            if (session == null) {
                logger.debug { "No connection available, initiating new one..." }
                initConnection()
                    .onSuccess {
                        logger.debug { "Initiated connection successfully" }
                        return Result.success(flow)
                    }
                    .onFailure {
                        logger.debug { "Connection initialization failed: $it" }
                        return Result.failure(it)
                    }
            } else Result.success(flow)
            return Result.failure(UnknownError())
        }
    }
}
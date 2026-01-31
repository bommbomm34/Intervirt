package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.AppEnv
import io.github.bommbomm34.intervirt.data.qemu.QemuMonitorSession
import io.github.bommbomm34.intervirt.exceptions.OSException
import io.github.bommbomm34.intervirt.exceptions.QmpException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.Socket
import java.util.concurrent.TimeUnit

class QemuClient(
    private val fileManager: FileManager,
    private val guestManager: GuestManager,
    private val appEnv: AppEnv
) {

    var running = false
    private var isRunningLoopJob: Job? = null
    private val logger = KotlinLogging.logger { }
    private lateinit var currentProcess: Process
    private var qemuMonitorSession: QemuMonitorSession? = null

    private val startAlpineVMCommands = buildList {
        add(fileManager.getQemuFile().absolutePath)
        if (appEnv.vmEnableKvm) add("-enable-kvm")
        addAll(
            listOf(
                "-smp", appEnv.vmCpu.toString(),
                "-drive", "file=${fileManager.getAlpineDisk().absolutePath}",
                "-m", appEnv.vmRam.toString(),
                "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:${appEnv.agentPort}-:55436,dns=9.9.9.9",
                "-qmp", "tcp:127.0.0.1:${appEnv.qemuMonitorPort},server,nowait",
                "-device", "e1000,netdev=net0",
                "-nographic"
            )
        )
    }

    suspend fun bootAlpine(): Result<Unit> = withContext(Dispatchers.IO) {
        logger.debug { "Booting Alpine Linux" }
        val builder = ProcessBuilder(*startAlpineVMCommands.toTypedArray())
        builder.directory(fileManager.getFile("qemu"))
        builder.redirectErrorStream(true)
        currentProcess = builder.start()
        BufferedReader(InputStreamReader(currentProcess.inputStream)).use { tempReader ->
            logger.debug { "Started VM process" }
//        logger.debug { "Output: " + currentProcess.inputStream.bufferedReader().readText() }
            if (currentProcess.isAlive) {
                logger.debug { "Waiting for availability" }
                delay(2000) // Wait for QEMU to start QMP
                initMonitorSocket()
                    .onSuccess { qemuMonitorSession = it }
                    .onFailure { return@withContext Result.failure(it) }
                isRunningLoop() // Runs in background
                while (!running) {
                    if (!currentProcess.isAlive) {
                        // QEMU start process failed
                        val error = OSException(tempReader.readText())
                        logger.error(error) { "Process exited unexpectedly" }
                        return@withContext Result.failure(error)
                    }
                    delay(1000)
                }
            }
            return@withContext if (currentProcess.isAlive) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException())
            }
        }
    }

    suspend fun shutdownAlpine() {
        logger.info { "Shutting down Alpine VM" }
        logger.debug { "Closing QEMU monitor session" }
        qemuMonitorSession?.close()
        runCatching {
            guestManager.shutdown()
                .onFailure {
                    withContext(Dispatchers.IO){
                        logger.error(it) { "Shutdown attempt through agent failed" }
                        currentProcess.destroy()
                        logger.debug { "Waiting for Alpine VM to shutdown" }
                        currentProcess.waitFor(appEnv.vmShutdownTimeout, TimeUnit.MILLISECONDS)
                        if (currentProcess.isAlive) {
                            logger.debug { "Timeout exceeded, forcing shutdown..." }
                            currentProcess.destroyForcibly()
                            currentProcess.waitFor()
                        }
                    }
                }
        }.onFailure {
            if (it is UninitializedPropertyAccessException) logger.debug { "Alpine VM is already offline." }
        }
        isRunningLoopJob?.cancel()
        isRunningLoopJob = null
        logger.debug { "Alpine VM is now offline" }
    }

    suspend fun addPortForwarding(protocol: String, hostPort: Int, guestPort: Int): Result<Unit> {
        qmpSend(buildJsonObject {
            put("execute", "human-monitor-command")
            putJsonObject("arguments") {
                put("command-line", "hostfwd_add net0 $protocol:127.0.0.1:$hostPort-:$guestPort")
            }
        }).fold(
            onSuccess = { return Result.success(Unit) },
            onFailure = { return Result.failure(it) }
        )
    }

    suspend fun removePortForwarding(protocol: String, hostPort: Int): Result<Unit> {
        qmpSend(buildJsonObject {
            put("execute", "human-monitor-command")
            putJsonObject("arguments") {
                put("command-line", "hostfwd_remove net0 $protocol:127.0.0.1:$hostPort")
            }
        }).fold(
            onSuccess = { return Result.success(Unit) },
            onFailure = { return Result.failure(it) }
        )
    }

    suspend fun qmpSend(command: String, session: QemuMonitorSession? = qemuMonitorSession) = qmpSend(
        json = buildJsonObject { put("execute", command) },
        session = session
    )

    @Suppress("UNCHECKED_CAST")
    suspend fun qmpSend(json: JsonElement, session: QemuMonitorSession? = qemuMonitorSession): Result<JsonElement> {
        val payload = Json.encodeToString(json)
        session?.withLock {
            logger.debug { "Send to QMP: $payload" }
            writeLine(payload)
            logger.debug { "Waiting for answer" }
            withTimeoutOrNull(appEnv.qemuMonitorTimeout) {
                while (true) {
                    readLine()?.let { line ->
                        logger.debug { "Received answer: $line" }
                        val obj = Json.decodeFromString<JsonObject>(line)
                        val returnObj = obj["return"]
                        val errorObj = obj["error"]
                        return@withTimeoutOrNull when {
                            returnObj != null -> Result.success(returnObj)
                            errorObj != null -> Result.failure(QmpException(Json.decodeFromJsonElement(errorObj.jsonObject)))
                            else -> Result.failure(SerializationException("Received JSON is not QMP-conform: $line"))
                        }
                    }
                }
            }?.let { anyValue ->
                return if (anyValue is Result<*>) anyValue as Result<JsonObject> else
                    Result.failure(IllegalStateException("Expected answer from QMP, but nothing received."))
            }
        }
        return Result.failure(NullPointerException("No QEMU Monitor session is available."))
    }

    private fun isRunningLoop() {
        if (isRunningLoopJob == null) {
            isRunningLoopJob = CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    running = qemuMonitorSession?.let { _ ->
                        val result = qmpSend("query-status")
                        logger.debug { "Result of query-status: $result" }
                        result.fold(
                            onSuccess = { it.jsonObject["running"]!!.jsonPrimitive.boolean },
                            onFailure = { false }
                        )
                    } ?: false
                    delay(2500)
                }
            }
        }
    }

    private fun testAgentPort(agentPort: Int): Boolean {
        try {
            Socket("127.0.0.1", agentPort).use { return true }
        } catch (_: ConnectException) {
            return false
        }
    }

    private suspend fun initMonitorSocket(): Result<QemuMonitorSession> = runCatching {
        logger.debug { "Initializing monitor socket connection" }
        val selector = ActorSelectorManager(Dispatchers.IO)
        return@runCatching withTimeout(5000) {
            val socket = aSocket(selector).tcp().connect("127.0.0.1", appEnv.qemuMonitorPort)
            val session = QemuMonitorSession(selector, socket)
            logger.debug { "Initialized session" }
            session.withLock { session.readLine() } // First message is just greeting
            logger.debug { "Negotiating capabilities with QMP" }
            // Negotiate capabilities
            qmpSend("qmp_capabilities", session).getOrThrow()
            qemuMonitorSession = session
            return@withTimeout session
        }
    }
}
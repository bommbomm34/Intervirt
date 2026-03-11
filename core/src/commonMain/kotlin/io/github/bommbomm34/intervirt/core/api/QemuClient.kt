/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.qemu.QemuMonitorSession
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.exceptions.OSException
import io.github.bommbomm34.intervirt.core.exceptions.QmpException
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.atomic
import io.github.bommbomm34.intervirt.core.util.getLogger
import io.github.bommbomm34.intervirt.core.withCatchingContext

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class QemuClient(
    private val fileManager: FileManager,
    private val guestManager: GuestManager,
    private val appEnv: AppEnv,
) : AsyncCloseable {

    var running by atomic(false) { value ->
        onRunningChangeListeners.forEach { it(value) }
    }
    private var isRunningLoopJob: Job? = null
    private val logger = appEnv.getLogger(QemuClient::class)
    private lateinit var currentProcess: Process
    private var qemuMonitorSession: QemuMonitorSession? = null
    private val onRunningChangeListeners = mutableListOf<(Boolean) -> Unit>()

    private val startAlpineVMCommands = buildList {
        add(fileManager.getQemuFile().absolutePath)
        if (appEnv.VM_ENABLE_KVM) add("-enable-kvm")
        addAll(
            listOf(
                "-smp", appEnv.VM_CPU.toString(),
                "-drive", "file=${fileManager.getAlpineDisk().absolutePath}",
                "-m", appEnv.VM_RAM.toString(),
                "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:${appEnv.AGENT_PORT}-:55436,dns=9.9.9.9",
                "-qmp", "tcp:127.0.0.1:${appEnv.QEMU_MONITOR_PORT},server,nowait",
                "-device", "e1000,netdev=net0",
                "-nographic",
            ),
        )
    }

    suspend fun bootAlpine(): Result<Unit> = withCatchingContext(Dispatchers.IO) {
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
                qemuMonitorSession = initMonitorSocket().getOrThrow()
                isRunningLoop() // Runs in background
                while (!running) {
                    if (!currentProcess.isAlive) {
                        // QEMU start process failed
                        val error = OSException(tempReader.readText())
                        logger.error(error) { "Process exited unexpectedly" }
                        throw error
                    }
                    delay(1000)
                }
            }
            if (!currentProcess.isAlive) throw IllegalStateException()
        }
        logger.debug { "Booted Alpine Linux" }
    }

    suspend fun shutdownAlpine() {
        logger.info { "Shutting down Alpine VM" }
        logger.debug { "Closing QEMU monitor session" }
        qemuMonitorSession?.close()
        runSuspendingCatching {
            guestManager.shutdown()
                .onFailure {
                    withContext(Dispatchers.IO) {
                        logger.error(it) { "Shutdown attempt through agent failed" }
                        currentProcess.destroy()
                        logger.debug { "Waiting for Alpine VM to shutdown" }
                        currentProcess.waitFor(appEnv.VM_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)
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
        running = false
        logger.debug { "Alpine VM is now offline" }
    }

    suspend fun addPortForwarding(protocol: String, externalPort: Int, internalPort: Int): Result<Unit> {
        val fwd = PortForwarding(protocol, externalPort, internalPort)
        require(fwd.validate()) { "Port forwarding is invalid: $fwd" }
        logger.debug { "Adding port forwarding $protocol:$externalPort:$internalPort" }
        return qmpSend(
            buildJsonObject {
                put("execute", "human-monitor-command")
                putJsonObject("arguments") {
                    put("command-line", "hostfwd_add net0 $protocol:127.0.0.1:$externalPort-:$internalPort")
                }
            },
        ).map {
            logger.debug { "Added port forwarding $protocol:$externalPort:$internalPort" }
        }
    }

    suspend fun removePortForwarding(protocol: String, externalPort: Int): Result<Unit> {
        require(protocol.isValidProtocol()) { "Invalid protocol $protocol" }
        require(externalPort.isValidPort()) { "Invalid external port $externalPort" }
        logger.debug { "Removing port forwarding $protocol:$externalPort" }
        return qmpSend(
            buildJsonObject {
                put("execute", "human-monitor-command")
                putJsonObject("arguments") {
                    put("command-line", "hostfwd_remove net0 $protocol:127.0.0.1:$externalPort")
                }
            },
        ).map {
            logger.debug { "Removed port forwarding $protocol:$externalPort" }
        }
    }

    suspend fun qmpSend(command: String, session: QemuMonitorSession? = qemuMonitorSession) = qmpSend(
        json = buildJsonObject { put("execute", command) },
        session = session,
    )

    @Suppress("UNCHECKED_CAST")
    suspend fun qmpSend(json: JsonElement, session: QemuMonitorSession? = qemuMonitorSession): Result<JsonElement> {
        val payload = defaultJson.encodeToString(json)
        session?.withLock {
            logger.debug { "Send to QMP: $payload" }
            writeLine(payload)
            logger.debug { "Waiting for answer" }
            withTimeoutOrNull(appEnv.QEMU_MONITOR_TIMEOUT.toLong()) {
                while (true) {
                    readLine()?.let { line ->
                        logger.debug { "Received answer: $line" }
                        val obj = defaultJson.decodeFromString<JsonObject>(line)
                        val returnObj = obj["return"]
                        val errorObj = obj["error"]
                        return@withTimeoutOrNull when {
                            returnObj != null -> Result.success(returnObj)
                            errorObj != null -> Result.failure(QmpException(defaultJson.decodeFromJsonElement(errorObj.jsonObject)))
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

    fun onRunningChange(block: (Boolean) -> Unit) = onRunningChangeListeners.add(block)

    private fun isRunningLoop() {
        if (isRunningLoopJob == null) {
            isRunningLoopJob = CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    running = qemuMonitorSession?.let { _ ->
                        val result = qmpSend("query-status")
                        logger.debug { "Result of query-status: $result" }
                        result.fold(
                            onSuccess = { it.jsonObject["running"]!!.jsonPrimitive.boolean },
                            onFailure = { false },
                        )
                    } ?: false
                    delay(2500)
                }
            }
        }
    }

    private suspend fun initMonitorSocket(): Result<QemuMonitorSession> = runSuspendingCatching {
        logger.debug { "Initializing monitor socket connection" }
        val selector = ActorSelectorManager(Dispatchers.IO)
        return@runSuspendingCatching withTimeout(5000) {
            val socket = aSocket(selector).tcp().connect("127.0.0.1", appEnv.QEMU_MONITOR_PORT)
            val session = QemuMonitorSession(selector, socket)
            logger.debug { "Initialized session" }
            session.withLock { session.readLine() } // First message is just greeting
            logger.debug { "Negotiating capabilities with QMP" }
            // Negotiate capabilities
            qmpSend("qmp_capabilities", session).getOrThrow()
            qemuMonitorSession = session
            logger.info { "Initialized monitor socket connection" }
            return@withTimeout session
        }
    }

    override suspend fun close() = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Closing QemuClient" }
        isRunningLoopJob?.cancel()
        shutdownAlpine()
        logger.debug { "Closed QemuClient" }
    }
}
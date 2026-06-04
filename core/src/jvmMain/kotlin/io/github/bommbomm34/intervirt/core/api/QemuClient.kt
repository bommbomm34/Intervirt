/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import arrow.core.right
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.qemu.QemuMonitorSession
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.error
import io.github.bommbomm34.intervirt.core.exceptions.OSException
import io.github.bommbomm34.intervirt.core.exceptions.QemuException
import io.github.bommbomm34.intervirt.core.exceptions.QmpException
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.atomic
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import io.github.vinceglb.filekit.absolutePath

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

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
        add(fileManager.getQemuFile().absolutePath())
        if (appEnv.VM_ENABLE_KVM) add("-enable-kvm")
        addAll(
            listOf(
                "-smp", appEnv.VM_CPU.toString(),
                "-drive", "file=${fileManager.getAlpineDisk().absolutePath()}",
                "-m", appEnv.VM_RAM.toString(),
                "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:${appEnv.AGENT_PORT}-:55436,dns=9.9.9.9",
                "-qmp", "tcp:127.0.0.1:${appEnv.QEMU_MONITOR_PORT},server,nowait",
                "-device", "e1000,netdev=net0",
                "-nographic",
            ),
        )
    }

    suspend fun bootAlpine(): AppResult<Unit> = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Booting Alpine Linux" }
        val builder = ProcessBuilder(*startAlpineVMCommands.toTypedArray())
        builder.directory(fileManager.getFile("qemu").file)
        builder.redirectErrorStream(true)
        currentProcess = builder.start()
        BufferedReader(InputStreamReader(currentProcess.inputStream)).use { tempReader ->
            logger.debug { "Started VM process" }
//        logger.debug { "Output: " + currentProcess.inputStream.bufferedReader().readText() }
            if (currentProcess.isAlive) {
                logger.debug { "Waiting for availability" }
                delay(2000.milliseconds) // Wait for QEMU to start QMP
                qemuMonitorSession = initMonitorSocket().bind()
                isRunningLoop() // Runs in background
                while (!running) {
                    if (!currentProcess.isAlive) {
                        // QEMU start process failed
                        val error = QemuException("QEMU start process failed")
                        logger.error(error) { "Process exited unexpectedly" }
                        throw error
                    }
                    delay(1000.milliseconds)
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
        either<Failure, Unit> {
            if (::currentProcess.isInitialized) {
                logger.debug { "Alpine VM is already initialized" }
                return@either
            }
            guestManager.shutdown()
                .onLeft {
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
        }
        isRunningLoopJob?.cancel()
        isRunningLoopJob = null
        running = false
        logger.debug { "Alpine VM is now offline" }
    }

    suspend fun addPortForwarding(protocol: String, externalPort: Int, internalPort: Int): AppResult<Unit> {
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

    suspend fun addPortForwarding(portForwarding: PortForwarding): AppResult<Unit> = addPortForwarding(
        protocol = portForwarding.protocol,
        externalPort = portForwarding.externalPort,
        internalPort = portForwarding.internalPort,
    )

    suspend fun removePortForwarding(protocol: String, externalPort: Int): AppResult<Unit> {
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

    suspend fun removePortForwarding(portForwarding: PortForwarding): AppResult<Unit> = removePortForwarding(
        protocol = portForwarding.protocol,
        externalPort = portForwarding.externalPort,
    )

    suspend fun qmpSend(command: String, session: QemuMonitorSession? = qemuMonitorSession) = qmpSend(
        json = buildJsonObject { put("execute", command) },
        session = session,
    )

    @Suppress("UNCHECKED_CAST")
    suspend fun qmpSend(json: JsonElement, session: QemuMonitorSession? = qemuMonitorSession): AppResult<JsonElement> {
        val payload = defaultJson.encodeToString(json)
        session?.withLock {
            logger.debug { "Send to QMP: $payload" }
            writeLine(payload)
            logger.debug { "Waiting for answer" }
            withTimeoutOrNull(appEnv.QEMU_MONITOR_TIMEOUT.milliseconds) {
                while (true) {
                    readLine()?.let { line ->
                        logger.debug { "Received answer: $line" }
                        val obj = defaultJson.decodeFromString<JsonObject>(line)
                        val returnObj = obj["return"]
                        val errorObj = obj["error"]
                        return@withTimeoutOrNull when {
                            returnObj != null -> returnObj.right()
                            errorObj != null -> Failure.Qmp(defaultJson.decodeFromJsonElement(errorObj.jsonObject)).left()
                            else -> Failure.Serialization("Received JSON is not QMP-conform: $line").left()
                        }
                    }
                }
            }?.let { anyValue ->
                return if (anyValue is Either<*, *>) anyValue as AppResult<JsonObject> else
                    Failure.IllegalState("Expected answer from QMP, but nothing received.").left()
            }
        }
        return Failure.IllegalState("No QEMU Monitor session is available.").left()
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
                            ifRight = { it.jsonObject["running"]!!.jsonPrimitive.boolean },
                            ifLeft = { false },
                        )
                    } ?: false
                    delay(2500.milliseconds)
                }
            }
        }
    }

    private suspend fun initMonitorSocket(): AppResult<QemuMonitorSession> = either {
        logger.debug { "Initializing monitor socket connection" }
        val selector = ActorSelectorManager(Dispatchers.IO)
        withTimeout(5000.milliseconds) {
            val socket = aSocket(selector).tcp().connect("127.0.0.1", appEnv.QEMU_MONITOR_PORT)
            val session = QemuMonitorSession(selector, socket)
            logger.debug { "Initialized session" }
            session.withLock { session.readLine() } // First message is just greeting
            logger.debug { "Negotiating capabilities with QMP" }
            // Negotiate capabilities
            qmpSend("qmp_capabilities", session).bind()
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

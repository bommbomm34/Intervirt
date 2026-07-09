/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.context.Raise
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.atomic.getValue
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.qemu.QemuMonitorSession
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.exceptions.QemuException
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.atomic
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import io.github.vinceglb.filekit.absolutePath
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class QemuClient(
    private val fileManager: FileManager,
    envHolder: AppEnvHolder,
) : AsyncCloseable {
    val appEnv by envHolder

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
        if (appEnv.vmEnableKvm) add("-enable-kvm")
        addAll(
            listOf(
                "-smp", appEnv.vmCpu.toString(),
                "-drive", "file=${fileManager.getAlpineDisk().absolutePath()}",
                "-m", appEnv.vmRam.toString(),
                "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:${appEnv.agentPort}-:55436,dns=9.9.9.9",
                "-qmp", "tcp:127.0.0.1:${appEnv.qemuMonitorPort},server,nowait",
                "-device", "e1000,netdev=net0",
                "-nographic",
            ),
        )
    }

    context(_: Raise<Failure>)
    suspend fun bootAlpine() = withCatchingContext(Dispatchers.IO) {
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
                qemuMonitorSession = initMonitorSocket()
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
        either {
            if (::currentProcess.isInitialized) {
                logger.debug { "Alpine VM is already initialized" }
                return@either
            }
            withContext(Dispatchers.IO) {
                qmpSend("stop")
                logger.debug { "Waiting for Alpine VM to shutdown" }
                currentProcess.waitFor(appEnv.vmShutdownTimeout, TimeUnit.MILLISECONDS)
                if (currentProcess.isAlive) {
                    logger.debug { "Timeout exceeded, forcing shutdown..." }
                    currentProcess.destroyForcibly()
                    currentProcess.waitFor()
                }
            }
        }
        isRunningLoopJob?.cancel()
        isRunningLoopJob = null
        running = false
        logger.debug { "Alpine VM is now offline" }
    }

    context(_: Raise<Failure>)
    suspend fun addPortForwarding(protocol: String, externalPort: Int, internalPort: Int) {
        val fwd = PortForwarding(protocol, externalPort, internalPort)
        require(fwd.validate()) { "Port forwarding is invalid: $fwd" }
        logger.debug { "Adding port forwarding $protocol:$externalPort:$internalPort" }
        qmpSend(
            buildJsonObject {
                put("execute", "human-monitor-command")
                putJsonObject("arguments") {
                    put("command-line", "hostfwd_add net0 $protocol:127.0.0.1:$externalPort-:$internalPort")
                }
            },
        ).also {
            logger.debug { "Added port forwarding $protocol:$externalPort:$internalPort" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun addPortForwarding(portForwarding: PortForwarding) = addPortForwarding(
        protocol = portForwarding.protocol,
        externalPort = portForwarding.externalPort,
        internalPort = portForwarding.internalPort,
    )

    context(_: Raise<Failure>)
    suspend fun removePortForwarding(protocol: String, externalPort: Int) {
        require(protocol.isValidProtocol()) { "Invalid protocol $protocol" }
        require(externalPort.isValidPort()) { "Invalid external port $externalPort" }
        logger.debug { "Removing port forwarding $protocol:$externalPort" }
        qmpSend(
            buildJsonObject {
                put("execute", "human-monitor-command")
                putJsonObject("arguments") {
                    put("command-line", "hostfwd_remove net0 $protocol:127.0.0.1:$externalPort")
                }
            },
        ).also {
            logger.debug { "Removed port forwarding $protocol:$externalPort" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun removePortForwarding(portForwarding: PortForwarding) = removePortForwarding(
        protocol = portForwarding.protocol,
        externalPort = portForwarding.externalPort,
    )

    context(_: Raise<Failure>)
    suspend fun qmpSend(command: String, session: QemuMonitorSession? = qemuMonitorSession) = qmpSend(
        json = buildJsonObject { put("execute", command) },
        session = session,
    )

    @Suppress("UNCHECKED_CAST")
    context(_: Raise<Failure>)
    suspend fun qmpSend(json: JsonElement, session: QemuMonitorSession? = qemuMonitorSession): JsonElement {
        val payload = defaultJson.encodeToString(json)
        session?.withLock {
            logger.debug { "Send to QMP: $payload" }
            writeLine(payload)
            logger.debug { "Waiting for answer" }
            withTimeoutOrNull(appEnv.qemuMonitorTimeout.milliseconds) {
                while (true) {
                    readLine()?.let { line ->
                        logger.debug { "Received answer: $line" }
                        val obj = defaultJson.decodeFromString<JsonObject>(line)
                        val returnObj = obj["return"]
                        val errorObj = obj["error"]
                        return@withTimeoutOrNull when {
                            returnObj != null -> returnObj
                            errorObj != null -> raise(Failure.Qmp(defaultJson.decodeFromJsonElement(errorObj.jsonObject)))
                            else -> raise(Failure.Serialization("Received JSON is not QMP-conform: $line"))
                        }
                    }
                }
            }?.let { anyValue ->
                return anyValue as? JsonObject ?: raise(Failure.IllegalState("Expected answer from QMP, but nothing received."))
            }
        }
        raise(Failure.IllegalState("No QEMU Monitor session is available."))
    }

    fun onRunningChange(block: (Boolean) -> Unit) = onRunningChangeListeners.add(block)

    private fun isRunningLoop() {
        if (isRunningLoopJob == null) {
            isRunningLoopJob = CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    running = qemuMonitorSession?.let { _ ->
                        recover<Failure, Boolean>(
                            block = {
                                val result = qmpSend("query-status")
                                logger.debug { "Result of query-status: $result" }
                                result.jsonObject["running"]!!.jsonPrimitive.boolean
                            },
                            recover = { false }
                        )
                    } ?: false
                    delay(2500.milliseconds)
                }
            }
        }
    }

    context(_: Raise<Failure>)
    private suspend fun initMonitorSocket(): QemuMonitorSession {
        logger.debug { "Initializing monitor socket connection" }
        val selector = ActorSelectorManager(Dispatchers.IO)
        return withTimeout(5000.milliseconds) {
            val socket = aSocket(selector).tcp().connect("127.0.0.1", appEnv.qemuMonitorPort)
            val session = QemuMonitorSession(selector, socket)
            logger.debug { "Initialized session" }
            session.withLock { session.readLine() } // First message is just greeting
            logger.debug { "Negotiating capabilities with QMP" }
            // Negotiate capabilities
            qmpSend("qmp_capabilities", session)
            qemuMonitorSession = session
            logger.info { "Initialized monitor socket connection" }
            session
        }
    }

    context(_: Raise<Failure>)
    override suspend fun close() = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Closing QemuClient" }
        isRunningLoopJob?.cancel()
        shutdownAlpine()
        logger.debug { "Closed QemuClient" }
    }
}

package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.QemuMonitorSession
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.readLine
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.Socket
import java.util.concurrent.TimeUnit

class QemuClient(
    private val fileManager: FileManager,
    private val agentClient: AgentClient,
    private val preferences: Preferences
) {

    private val logger = KotlinLogging.logger { }
    private lateinit var currentProcess: Process
    private lateinit var qemuMonitorSession: QemuMonitorSession

    private val startAlpineVMCommands = listOf(
        fileManager.getQemuFile().absolutePath,
        if (preferences.VM_ENABLE_KVM) "-enable-kvm" else "",
        "-smp", preferences.VM_CPU.toString(),
        "-drive", "file=../disk/alpine-linux.qcow2,format=qcow2",
        "-m", preferences.VM_RAM.toString(),
        "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:${preferences.AGENT_PORT}-:55436,dns=9.9.9.9",
        "-monitor", "tcp:127.0.0.1:${preferences.QEMU_MONITOR_PORT},server,nowait",
        "-device", "e1000,netdev=net0",
        "-nographic"
    )

    suspend fun bootAlpine(): Result<Boolean> {
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
                val startTime = System.currentTimeMillis()
                while (!testAgentPort(preferences.AGENT_PORT)) {
                    if (System.currentTimeMillis() - startTime > preferences.AGENT_TIMEOUT) {
                        return Result.failure(IllegalStateException("Agent isn't available"))
                    }
                    delay(500)
                }
            }
            return if (currentProcess.isAlive) {
                initMonitorSocket().fold(
                    onSuccess = { Result.success(true) },
                    onFailure = { Result.failure(it) }
                )
            } else {
                Result.failure(IllegalStateException())
            }
        }
    }

    suspend fun shutdownAlpine() {
        logger.info { "Shutting down Alpine VM" }
        agentClient.shutdown()
            .onFailure {
                logger.error { "Shutdown attempt through agent failed: $it" }
                logger.debug { "Shutdown through process termination" }
                currentProcess.destroy()
                logger.debug { "Waiting for Alpine VM to shutdown" }
                currentProcess.waitFor(preferences.VM_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)
                if (currentProcess.isAlive) {
                    logger.debug { "Timeout exceeded, forcing shutdown..." }
                    currentProcess.destroyForcibly()
                    currentProcess.waitFor()
                }
            }
        logger.debug { "Alpine VM is now offline" }
    }
    fun isRunning() = this::currentProcess.isInitialized && currentProcess.isAlive

    fun monitorSend(command: String): Flow<String> = flow {
        logger.debug { "Send $command to monitor" }
        qemuMonitorSession.writeChannel.writeStringUtf8(command)
        while (true){
            val line = qemuMonitorSession.readChannel.readLine()
            line?.let {
                if (it.contains("(qemu)")) break else emit(line)
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

    private suspend fun initMonitorSocket(): Result<Unit> = runCatching {
        val selector = ActorSelectorManager(Dispatchers.IO)
        val socket = aSocket(selector).tcp().connect("127.0.0.1", preferences.QEMU_MONITOR_PORT)
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)
        qemuMonitorSession = QemuMonitorSession(input, output)
        while (true){
            val line = input.readLine()
            if (line?.contains("(qemu)") ?: false) break
        }
    }
}


package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.api.Preferences
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
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
    private val logger = KotlinLogging.logger {  }
    private var currentProcess: Process? = null
    private val startAlpineVmCommands = listOf(
        fileManager.getQemuFile().absolutePath,
        if (preferences.VM_ENABLE_KVM) "-enable-kvm" else "",
        "-smp", preferences.VM_CPU.toString(),
        "-drive", "file=../disk/alpine-linux.qcow2,format=qcow2",
        "-m", preferences.VM_RAM.toString(),
        "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:${preferences.AGENT_PORT}-:55436,dns=9.9.9.9",
        "-device", "e1000,netdev=net0",
        "-nographic"
    )

    suspend fun bootAlpine(): Result<Boolean> {
        logger.debug { "Booting Alpine Linux" }
        val builder = ProcessBuilder(*startAlpineVmCommands.toTypedArray())
        builder.directory(fileManager.getFile("qemu"))
        builder.redirectErrorStream(true)
        currentProcess = builder.start()
        BufferedReader(InputStreamReader(currentProcess!!.inputStream)).use { tempReader ->
            logger.debug { "Started VM process" }
//        logger.debug { "Output: " + currentProcess!!.inputStream.bufferedReader().readText() }
            if (currentProcess!!.isAlive) {
                logger.debug { "Waiting for availability" }
                val startTime = System.currentTimeMillis()
                while (!testAgentPort(preferences.AGENT_PORT)) {
                    if (System.currentTimeMillis() - startTime > preferences.SSH_TIMEOUT) {
                        return Result.failure(IllegalStateException("SSH isn't available"))
                    }
                    delay(500)
                }
            }
            return if (currentProcess!!.isAlive) {
                Result.success(true)
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
                currentProcess?.destroy()
                logger.debug { "Waiting for Alpine VM to shutdown" }
                currentProcess?.waitFor(preferences.VM_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)
                if (currentProcess?.isAlive ?: false) {
                    logger.debug { "Timeout exceeded, forcing shutdown..." }
                    currentProcess?.destroyForcibly()
                    currentProcess?.waitFor()
                }
            }
        logger.debug { "Alpine VM is now offline" }
        currentProcess = null
    }

    fun isRunning() = currentProcess != null

    private fun testAgentPort(agentPort: Int): Boolean {
        try {
            Socket("127.0.0.1", agentPort).use { return true }
        } catch (_: ConnectException) {
            return false
        }
    }
}


package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.*
import io.github.bommbomm34.intervirt.data.FileManager
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.Socket
import java.util.concurrent.TimeUnit

object QEMUClient {
    val logger = KotlinLogging.logger {  }
    var currentProcess: Process? = null

    suspend fun bootAlpine(): Result<Boolean> {
        logger.debug { "Booting Alpine Linux" }
        val builder = ProcessBuilder(*START_ALPINE_VM_COMMANDS.toTypedArray())
        builder.directory(FileManager.getFile("qemu"))
        builder.redirectErrorStream(true)
        currentProcess = builder.start()
        BufferedReader(InputStreamReader(currentProcess!!.inputStream)).use { tempReader ->
            logger.debug { "Started VM process" }
//        logger.debug { "Output: " + currentProcess!!.inputStream.bufferedReader().readText() }
            if (currentProcess!!.isAlive) {
                logger.debug { "Waiting for availability" }
                val startTime = System.currentTimeMillis()
                while (!testAgentPort()) {
                    if (System.currentTimeMillis() - startTime > SSH_TIMEOUT) {
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
        AgentClient.shutdown()
            .onFailure {
                logger.error { "Shutdown attempt through agent failed: $it" }
                logger.debug { "Shutdown through process termination" }
                currentProcess?.destroy()
                logger.debug { "Waiting for Alpine VM to shutdown" }
                currentProcess?.waitFor(VM_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)
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
}

fun testAgentPort(): Boolean {
    try {
        Socket("127.0.0.1", AGENT_PORT).use { return true }
    } catch (_: ConnectException) {
        return false
    }
}
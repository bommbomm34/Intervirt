package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.SSH_PORT
import io.github.bommbomm34.intervirt.SSH_TIMEOUT
import io.github.bommbomm34.intervirt.START_ALPINE_VM_COMMANDS
import io.github.bommbomm34.intervirt.data.Executor
import io.github.bommbomm34.intervirt.data.FileManagement
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.Socket

class QEMUInterface(val fileManagement: FileManagement, val executor: Executor) {
    var currentProcess: Process? = null

    suspend fun bootAlpine(): Result<Boolean> {
        logger.debug { "Booting Alpine Linux" }
        val builder = ProcessBuilder(*START_ALPINE_VM_COMMANDS.toTypedArray())
        builder.directory(fileManagement.getFile("qemu"))
        builder.redirectErrorStream(true)
        currentProcess = builder.start()
        BufferedReader(InputStreamReader(currentProcess!!.inputStream)).use { tempReader ->
            logger.debug { "Started VM process" }
//        logger.debug { "Output: " + currentProcess!!.inputStream.bufferedReader().readText() }
            if (currentProcess!!.isAlive) {
                logger.debug { "Waiting for availability" }
                val startTime = System.currentTimeMillis()
                while (!testSSHPort(SSH_PORT)) {
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
        logger.debug { "Shutting down Alpine VM" }
        executor.runCommandOnGuest("poweroff").collect {  }
        logger.debug { "Waiting for Alpine VM to shutdown" }
        currentProcess?.waitFor()
        currentProcess = null
    }
}

fun testSSHPort(port: Int): Boolean {
    try {
        Socket("127.0.0.1", port).use { socket ->
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val line = reader.readLine()
            return line != null && line.startsWith("SSH")
        }
    } catch (_: ConnectException){
        return false
    }
}
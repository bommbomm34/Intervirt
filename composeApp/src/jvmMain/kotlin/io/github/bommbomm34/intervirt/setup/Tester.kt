package io.github.bommbomm34.intervirt.setup

import io.github.bommbomm34.intervirt.DEBUG_ENABLED
import io.github.bommbomm34.intervirt.api.QEMUInterface
import io.github.bommbomm34.intervirt.data.Executor
import io.github.bommbomm34.intervirt.data.FileManagement
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.toList

// Tests installation
class Tester(val fileManagement: FileManagement, val executor: Executor) {
    suspend fun testQEMUInstallation(): Result<String> {
        logger.debug { "Testing QEMU" }
        val qemu = fileManagement.getQEMUFile().name
        val output = executor.runCommandOnHost("qemu", "./$qemu", "--version").toList()
        val outputString = output.joinToString("\n")
        logger.debug { "Output: $outputString" }
        return if (output.last().statusCode == 0) Result.success(outputString) else Result.failure(
            IllegalStateException(
                outputString
            )
        )
    }

    suspend fun testAlpineLinuxBoot(): Result<String> {
        val output = StringBuilder()
        logger.debug { "Testing booting Alpine Linux" }
        val qemu = QEMUInterface(fileManagement, executor)
        try {
            qemu.bootAlpine()
            logger.debug { "Running test command" }
            executor.runCommandOnGuest("echo Hello World").collect { output.append(it) }
            return if (output.isBlank()) Result.failure(IllegalStateException("Expected output, but no received")) else
                Result.success(output.toString())
        } catch (e: Exception){
            qemu.shutdownAlpine(DEBUG_ENABLED)
            throw e
        }
    }
}

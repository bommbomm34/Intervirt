package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.getCommandResult
import io.github.bommbomm34.intervirt.exceptions.ContainerExecutionException

// Simple wrapper for systemd
class SystemServiceManager(
    private val ioClient: ContainerIOClient
) {
    suspend fun start(name: String): Result<Unit> = exec("systemctl", "start", name).map { }

    suspend fun stop(name: String): Result<Unit> = exec("systemctl", "stop", name).map { }

    // TODO: Return a structured data class instead a single string
    suspend fun status(name: String): Result<String> = exec("systemctl", "status", "name")

    private suspend fun exec(vararg commands: String): Result<String> {
        val res = ioClient.exec(commands.toList())
        return res.fold(
            onSuccess = {
                val (output, statusCode) = it.getCommandResult()
                if (statusCode == 0) Result.success(output) else Result.failure(ContainerExecutionException(output))
            },
            onFailure = { Result.failure(it) }
        )
    }
}
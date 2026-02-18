package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.SystemServiceStatus
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.exceptions.ContainerExecutionException
import io.github.oshai.kotlinlogging.KotlinLogging

// Simple wrapper for systemd
class SystemServiceManager(
    private val ioClient: ContainerIOClient,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun start(name: String): Result<Unit> = exec("systemctl", "start", name).map { }

    suspend fun stop(name: String): Result<Unit> = exec("systemctl", "stop", name).map { }

    suspend fun restart(name: String): Result<Unit> = exec("systemctl", "restart", name).map { }

    suspend fun status(name: String) = exec("systemctl", "show", "--no-pager", name).map { raw ->
        val map = raw.lines()
            .associate { it.substringBefore("=") to it.substringAfter("=") }
        val status = SystemServiceStatus(
            enabled = map["UnitFileState"] == "enabled",
            active = map["ActiveState"] == "active",
        )
        logger.debug { "Status of $name: Active: ${status.active}, Enabled: ${status.enabled}" }
        status
    }

    private suspend fun exec(vararg commands: String): Result<String> {
        val res = ioClient.exec(commands.toList())
        return res.fold(
            onSuccess = {
                val (output, statusCode) = it.getCommandResult()
                if (statusCode == 0) Result.success(output) else Result.failure(ContainerExecutionException(output))
            },
            onFailure = { Result.failure(it) },
        )
    }
}
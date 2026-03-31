/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.SystemServiceStatus
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.exceptions.ContainerExecutionException
import io.github.bommbomm34.intervirt.core.exceptions.DockerContainerExecutionException
import io.github.bommbomm34.intervirt.core.util.ext.addFirst
import io.github.bommbomm34.intervirt.core.util.ext.getLogger


// Simple wrapper for systemd
class SystemServiceManager(
    appEnv: AppEnv,
    private val ioClient: ContainerIOClient,
) {
    private val logger = appEnv.getLogger(SystemServiceManager::class, ioClient.id)

    suspend fun start(name: String): Result<Unit> {
        logger.debug { "Starting system service $name" }
        return exec("systemctl", "start", name).map {
            logger.debug { "Started system service $name" }
        }
    }

    suspend fun stop(name: String): Result<Unit> {
        logger.debug { "Stopping system service $name" }
        return exec("systemctl", "stop", name).map {
            logger.debug { "Stopped system service $name" }
        }
    }

    suspend fun restart(name: String): Result<Unit> {
        logger.debug { "Restarting system service $name" }
        return exec("systemctl", "restart", name).map {
            logger.debug { "Restarted system service $name" }
        }
    }

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
        val res = ioClient.exec(commands.toList().addFirst("sudo"))
        return res.fold(
            onSuccess = {
                val (output, statusCode) = it.getCommandResult()
                if (statusCode == 0) Result.success(output) else Result.failure(DockerContainerExecutionException(output))
            },
            onFailure = { Result.failure(it) },
        )
    }
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.left
import arrow.core.raise.context.Raise
import arrow.core.raise.context.raise
import arrow.core.right
import io.github.bommbomm34.intervirt.core.data.AppEnv

import io.github.bommbomm34.intervirt.core.data.Failure
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

    context(_: Raise<Failure>)
    suspend fun start(name: String) {
        logger.debug { "Starting system service $name" }
        return exec("systemctl", "start", name).let {
            logger.debug { "Started system service $name" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun stop(name: String) {
        logger.debug { "Stopping system service $name" }
        return exec("systemctl", "stop", name).let {
            logger.debug { "Stopped system service $name" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun restart(name: String) {
        logger.debug { "Restarting system service $name" }
        return exec("systemctl", "restart", name).let {
            logger.debug { "Restarted system service $name" }
        }
    }

    context(_: Raise<Failure>)
    suspend fun status(name: String) = exec("systemctl", "show", "--no-pager", name).let { raw ->
        val map = raw.lines()
            .associate { it.substringBefore("=") to it.substringAfter("=") }
        val status = SystemServiceStatus(
            enabled = map["UnitFileState"] == "enabled",
            active = map["ActiveState"] == "active",
        )
        logger.debug { "Status of $name: Active: ${status.active}, Enabled: ${status.enabled}" }
        status
    }

    context(_: Raise<Failure>)
    private suspend fun exec(vararg commands: String): String {
        val flow = ioClient.exec(commands.toList().addFirst("sudo"))
        val (output, statusCode) = flow.getCommandResult()
        return if (statusCode == 0) output else raise(Failure.ContainerExecution(output))
    }
}

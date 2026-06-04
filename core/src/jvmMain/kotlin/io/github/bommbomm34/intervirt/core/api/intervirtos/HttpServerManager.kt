/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos

import arrow.core.raise.context.bind
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerBasedManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.exceptions.ContainerExecutionException
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext

import kotlinx.coroutines.Dispatchers
import kotlin.io.path.writeText

class HttpServerManager(
    appEnv: AppEnv,
    osClient: IntervirtOSClient,
) : DockerBasedManager(
    appEnv = appEnv,
    osClient = osClient,
    containerName = "apache2",
    containerImage = "ubuntu/apache2",
    portForwardings = listOf(PortForwarding("tcp", 80, 80)),
    volumes = mapOf("./" to "/etc/apache2"),
) {
    val docker = client.docker
    private val ioClient = client.ioClient
    private val logger = appEnv.getLogger(HttpServerManager::class)

    suspend fun loadHttpConf(conf: String): AppResult<Unit> = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Loading Apache2 configuration" }
        logger.debug { "Uploading Apache2 configuration" }
        ioClient.getPath("/opt/intervirt/apache2/sites-available/intervirt.conf").writeText(conf)
        logger.debug { "Enabling Apache2 configuration" }
        val flow = docker.exec(id, listOf("/usr/bin/a2ensite", "intervirt.conf")).bind()
        val (output, statusCode) = flow.getCommandResult()
        if (statusCode != 0) {
            logger.error { "Failed to enable Apache2 configuration: $output" }
            throw ContainerExecutionException(output)
        } else {
            logger.debug { "Reloading Apache2 configuration" }
            docker.restartContainer(id).bind()
        }
    }
}

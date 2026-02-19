package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.exceptions.ContainerExecutionException
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.withCatchingContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

class HttpServerManager(
    osClient: IntervirtOSClient,
) {
    private val client = osClient.getClient()
    val docker = client.docker
    private val ioClient = client.ioClient
    private val logger = KotlinLogging.logger { }
    private var id: String? = null

    suspend fun init(): Result<String> = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Initializing HTTP server manager" }
        val potentialId = docker.getContainer("apache2").getOrThrow()
        potentialId?.let { return@withCatchingContext it }
        // Create new container
        val hostPath = ioClient.getPath("/opt/intervirt/apache2")
            .createParentDirectories()
            .createDirectory()
        val newId = docker.addContainer(
            name = "apache2",
            image = "ubuntu/apache2",
            portForwardings = listOf(PortForwarding("tcp", 80, 80)),
            volumes = mapOf(hostPath.absolutePathString() to "/etc/apache2")
        ).getOrThrow()
        id = newId
        newId
    }

    suspend fun loadHttpConf(conf: String): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        val id = getId()
        logger.debug { "Loading Apache2 configuration" }
        logger.debug { "Uploading Apache2 configuration" }
        ioClient.getPath("/opt/intervirt/apache2/sites-available/intervirt.conf").writeText(conf)
        logger.debug { "Enabling Apache2 configuration" }
        val flow = docker.exec(id, listOf("/usr/bin/a2ensite", "intervirt.conf")).getOrThrow()
        val (output, statusCode) = flow.getCommandResult()
        if (statusCode != 0) {
            logger.error { "Failed to enable Apache2 configuration: $output" }
            throw ContainerExecutionException(output)
        } else {
            logger.debug { "Reloading Apache2 configuration" }
            docker.restartContainer(id).getOrThrow()
        }
    }

    private fun getId(): String {
        val idClone = id
        require(idClone != null) { "HTTP server manager isn't initialized" }
        return idClone
    }
}
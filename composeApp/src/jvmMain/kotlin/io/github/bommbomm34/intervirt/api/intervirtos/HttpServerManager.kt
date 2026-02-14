package io.github.bommbomm34.intervirt.api.intervirtos

import io.github.bommbomm34.intervirt.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.api.ContainerIOClient
import io.github.bommbomm34.intervirt.api.SystemServiceManager
import io.github.bommbomm34.intervirt.data.getCommandResult
import io.github.bommbomm34.intervirt.exceptions.ContainerExecutionException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.path.writeText

class HttpServerManager(
    bundle: ContainerClientBundle
) {
    val serviceManager = bundle.serviceManager
    private val ioClient = bundle.ioClient
    private val logger = KotlinLogging.logger {  }

    suspend fun loadHttpConf(conf: String): Result<Unit> {
        logger.debug { "Loading Apache2 configuration" }
        logger.debug { "Uploading Apache2 configuration" }
        runCatching {
            ioClient.getPath("/etc/apache2/sites-available/intervirt.conf").writeText(conf)
        }.onFailure { return Result.failure(it) }
        logger.debug { "Enabling Apache2 configuration" }
        return ioClient.exec(listOf("/usr/bin/a2ensite", "intervirt.conf")).fold(
            onSuccess = { flow ->
                val (output, statusCode) = flow.getCommandResult()
                if (statusCode != 0) {
                    logger.error { "Failed to enable Apache2 configuration: $output" }
                    Result.failure(ContainerExecutionException(output))
                } else {
                    logger.debug { "Reloading Apache2 configuration" }
                    serviceManager.restart("apache2")
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}
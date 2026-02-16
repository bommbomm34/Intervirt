package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.exceptions.ContainerExecutionException
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.path.notExists
import kotlin.io.path.readLines
import kotlin.io.path.writeText

class MailServerManager(
    osClient: IntervirtOSClient
) {
    private val client = osClient.getClient()
    val serviceManager = client.serviceManager
    private val ioClient = client.ioClient
    private val logger = KotlinLogging.logger {  }

    suspend fun listMailUsers(): Result<List<MailUser>> {
        logger.debug { "Listing mail users" }
        return ioClient.exec(listOf("/usr/bin/getent", "group", "intervirt_mail")).fold(
            onSuccess = { flow ->
                val (output, statusCode) = flow.getCommandResult()
                if (statusCode != 0) {
                    logger.error { "Failed to list mail users: $output" }
                    Result.failure(ContainerExecutionException(output))
                } else runCatching {
                    output.lines().map {
                        val name = it.split(":")[3]
                        val addressFile = ioClient.getPath("/home/$name/.intervirt_mail_address")
                        if (addressFile.notExists()) {
                            val error = "Missing mail address file for user $name"
                            logger.error { error }
                            error(error)
                        }
                        MailUser(name, addressFile.readLines()[0])
                    }
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun removeMailUser(name: String): Result<Unit> {
        logger.debug { "Remove mail user $name" }
        return ioClient.exec(listOf("deluser", name)).fold(
            onSuccess = { flow ->
                val (output, statusCode) = flow.getCommandResult()
                if (statusCode != 0) {
                    logger.error { "Error during removing user $name: $output" }
                    Result.failure(ContainerExecutionException(output))
                } else Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }

    // TODO: Accept password in a more secure way
    suspend fun addMailUser(user: MailUser, password: String): Result<Unit> {
        logger.debug { "Add mail user ${user.username} with email ${user.address}" }
        val command = listOf(
            "useradd",
            "-m",
            "-p", "$(openssl passwd -6 \"$password\")",
            user.username
        )
        return ioClient.exec(command).fold(
            onSuccess = { flow ->
                val (output, statusCode) = flow.getCommandResult()
                if (statusCode != 0) {
                    logger.error { "Error during adding mail user ${user.username}: $output" }
                    Result.failure(ContainerExecutionException(output))
                } else runCatching {
                    ioClient.getPath("/home/${user.username}/.intervirt_mail_address").writeText(user.address)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}
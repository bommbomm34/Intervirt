package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.Address
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.MailUser
import io.github.bommbomm34.intervirt.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.dns.DnsResolverOutput
import io.github.bommbomm34.intervirt.data.getCommandResult
import io.github.bommbomm34.intervirt.exceptions.ContainerExecutionException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlin.io.path.notExists
import kotlin.io.path.readLines
import kotlin.io.path.writeText

private val json = Json {
    ignoreUnknownKeys = true
}

class IntervirtOSClient(
    private val ioClient: ContainerIOClient,
    private val computer: Device.Computer
) {
    val serviceManager = SystemServiceManager(ioClient)
    private val logger = KotlinLogging.logger { }

    suspend fun lookupDns(
        name: String,
        type: String,
        nameserver: String,
        reverse: Boolean
    ): List<DnsRecord> {
        val baseCommandList = listOf("/usr/bin/doggo", name, "--type", type, "--nameserver", nameserver, "--json")
        val commandList = if (reverse) baseCommandList + "-x" else baseCommandList
        logger.debug { "Execute command \"${commandList.joinToString(" ")}\" for DNS lookup" }
        return ioClient.exec(
            commands = commandList
        ).fold(
            onSuccess = { flow ->
                val (output, statusCode) = flow.getCommandResult()
                logger.debug { "Received during DNS lookup:\n$output" }
                if (statusCode == 0) {
                    val resolverOutput = json.decodeFromString<DnsResolverOutput>(output)
                    resolverOutput.responses
                        .getOrNull(0)
                        ?.answers
                        ?.map { it.toDnsRecord() }
                        ?: emptyList()
                } else {
                    logger.error { "Unexpected status code during DNS lookup: $statusCode" }
                    emptyList()
                }
            },
            onFailure = { emptyList() }
        )
    }

    suspend fun getProxyUrl(
        deviceManager: DeviceManager
    ): Result<Address> {
        val port = getFreePort()
        return deviceManager.addPortForwarding(
            device = computer,
            internalPort = 1080,
            externalPort = port,
            protocol = "tcp"
        ).map { Address("127.0.0.1", port) }
    }

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

    // Mail Server
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
package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerBasedManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSStore
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.parseMailAddress
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.oshai.kotlinlogging.KotlinLogging

class MailServerManager(
    osClient: IntervirtOSClient,
) : DockerBasedManager(
    osClient = osClient,
    containerName = "mailserver",
    containerImage = "ghcr.io/docker-mailserver/docker-mailserver:latest",
    portForwardings = listOf(
        PortForwarding("tcp", 25, 25), // SMTP
        PortForwarding("tcp", 143, 143), // IMAP
    ),
    volumes = mapOf(
        "./docker-data/dms/mail-data/" to "/var/mail/",
        "./docker-data/dms/mail-state/" to "/var/mail-state/",
        "./docker-data/dms/mail-logs/" to "/var/log/mail/",
        "./docker-data/dms/config/" to "/tmp/docker-mailserver/",
    ),
    hostName = osClient.getClient().store[IntervirtOSStore.Accessor.HOSTNAME] ?: osClient.getClient().computer.id,
    env = mapOf(
        "OVERRIDE_HOSTNAME" to "ideapad.west-quillback.ts.net",
        "ACCOUNT_PROVISIONER" to "FILE",
    )
) {
    val docker = client.docker
    private val logger = KotlinLogging.logger { }

    suspend fun listMailUsers(): Result<List<MailUser>> = runSuspendingCatching {
        logger.debug { "Listing mail users" }
        val flow = docker.exec(id, listOf("setup", "email", "list")).getOrThrow()
        val output = flow.getCommandResult()
            .asResult()
            .getOrThrow()
        // Parse output
        output
            .lines()
            .filter { it.startsWith("*") }
            .map {
                val user = it
                    .substringAfter("* ")
                    .substringBefore(" ")
                    .parseMailAddress()
                logger.debug { "Listed mail user '$user'" }
                user
            }
    }

    suspend fun removeMailUser(user: MailUser): Result<Unit> = runSuspendingCatching {
        logger.debug { "Remove mail user $user" }
        docker
            .exec(id, listOf("setup", "email", "del", user.address))
            .getOrThrow()
            .getCommandResult()
            .asResult()
            .getOrThrow()
    }

    suspend fun addMailUser(user: MailUser, password: String): Result<Unit> = runSuspendingCatching {
        logger.debug { "Add mail user ${user.username} with email ${user.address}" }
        // TODO: Check if this method is secure
        val command = listOf(
            "setup",
            "email",
            "add",
            user.address,
            password,
        )
        docker
            .exec(id, command)
            .getOrThrow()
            .getCommandResult()
            .asResult()
            .getOrThrow()
    }
}
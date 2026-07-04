/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos

import arrow.core.raise.context.Raise
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerBasedManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSStore
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure

import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.bind
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.parseMailAddress


class MailServerManager(
    appEnv: AppEnv,
    osClient: IntervirtOSClient,
) : DockerBasedManager(
    appEnv = appEnv,
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
        "OVERRIDE_HOSTNAME" to (osClient.getClient().store[IntervirtOSStore.Accessor.HOSTNAME]
            ?: osClient.getClient().computer.id),
        "ACCOUNT_PROVISIONER" to "FILE",
    ),
) {
    val docker = client.docker
    private val logger = appEnv.getLogger(MailServerManager::class)

    context(_: Raise<Failure>)
    suspend fun listMailUsers(): List<MailUser> {
        logger.debug { "Listing mail users" }
        val flow = docker.exec(id, listOf("setup", "email", "list"))
        val output = flow.getCommandResult().bind()
            
        // Parse output
        return output
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

    context(_: Raise<Failure>)
    suspend fun removeMailUser(user: MailUser) {
        logger.debug { "Remove mail user $user" }
        docker
            .exec(id, listOf("setup", "email", "del", user.address))
            
            .getCommandResult()
            .bind()
    }

    context(_: Raise<Failure>)
    suspend fun addMailUser(user: MailUser, password: String) {
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
            
            .getCommandResult()
            .bind()
    }
}

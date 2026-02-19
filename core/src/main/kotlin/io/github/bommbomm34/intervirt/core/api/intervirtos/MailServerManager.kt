package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.parseMailAddress
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories

class MailServerManager(
    osClient: IntervirtOSClient,
) {
    private val client = osClient.getClient()
    val serviceManager = client.serviceManager
    private val ioClient = client.ioClient
    private val docker = client.docker
    private var id: String? = null
    private val logger = KotlinLogging.logger { }

    suspend fun init(): Result<String> = runSuspendingCatching {
        val potentialId = docker.getContainer("mailserver").getOrThrow()
        potentialId?.let { return@runSuspendingCatching it }
        // Create new container
        val hostPath = ioClient.getPath("/opt/intervirt/mailserver")
            .createParentDirectories()
            .createDirectory()
        val newId = docker.addContainer(
            name = "mailserver",
            image = "mailserver/docker-mailserver",
            portForwardings = listOf(
                PortForwarding("tcp", 25, 25), // SMTP
                PortForwarding("tcp", 143, 143), // IMAP
            ),
            volumes = mapOf(hostPath.absolutePathString() to "/etc/apache2"),
        ).getOrThrow()
        id = newId
        newId
    }

    suspend fun listMailUsers(): Result<List<MailUser>> = runSuspendingCatching {
        logger.debug { "Listing mail users" }
        val flow = docker.exec(getId(), listOf("setup", "email", "list")).getOrThrow()
        val output = flow.getCommandResult()
            .asResult()
            .getOrThrow()
        // Parse output
        output
            .lines()
            .filter { it.startsWith("*") }
            .map {
                it
                    .substringAfter("* ")
                    .substringBefore(" ")
                    .parseMailAddress()
            }
    }

    suspend fun removeMailUser(user: MailUser): Result<Unit> = runSuspendingCatching {
        logger.debug { "Remove mail user $user" }
        docker
            .exec(getId(), listOf("setup", "email", "del", user.address))
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
            .exec(getId(), command)
            .getOrThrow()
            .getCommandResult()
            .asResult()
            .getOrThrow()
    }

    fun getId(): String {
        val idClone = id
        require(idClone != null) { "Mail server manager isn't initialized" }
        return idClone
    }
}
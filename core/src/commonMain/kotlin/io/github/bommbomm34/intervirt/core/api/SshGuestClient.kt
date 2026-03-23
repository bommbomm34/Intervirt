/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.ext.exec
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession

class SshGuestClient(
    private val qemuClient: QemuClient,
    appEnv: AppEnv,
) : AsyncCloseable {
    private val sshClient = SshClient.setUpDefaultClient()
    private lateinit var session: ClientSession
    private lateinit var fwd: PortForwarding
    private val logger = appEnv.getLogger(SshGuestClient::class)

    var isInitialized = false

    suspend fun init(): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Initializing SshGuestClient" }
        initPortForwarding().getOrThrow()
        sshClient.start()
        session = sshClient.connect("intervirt", "127.0.0.1", fwd.externalPort)
            .verify()
            .session
        session.auth().verify()
        logger.info { "Initialized SSH connection with guest" }
        isInitialized = true
    }

    fun runCommand(vararg commands: String): Result<Flow<CommandStatus>> {
        val command = commands.joinToString(" ")
        logger.info { "Executing '$command' on guest" }
        return session.exec(command)
    }

    private suspend fun initPortForwarding(): Result<Unit> = runSuspendingCatching {
        val freePort = getFreePort()
        fwd = PortForwarding(
            protocol = "tcp",
            internalPort = 22,
            externalPort = freePort,
        )
        qemuClient.addPortForwarding(fwd).getOrThrow()
    }

    override suspend fun close(): Result<Unit> = runSuspendingCatching {
        session.close()
        sshClient.close()
        qemuClient.removePortForwarding(fwd).getOrThrow()
    }
}
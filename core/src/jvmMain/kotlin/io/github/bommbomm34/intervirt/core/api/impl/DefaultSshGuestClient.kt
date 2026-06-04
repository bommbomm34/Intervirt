/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.api.SshGuestClient
import io.github.bommbomm34.intervirt.core.api.getFreePort
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.ext.exec
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession

class DefaultSshGuestClient(
    private val qemuClient: QemuClient,
    appEnv: AppEnv,
) : SshGuestClient {
    private val sshClient = SshClient.setUpDefaultClient()
    private lateinit var session: ClientSession
    private lateinit var fwd: PortForwarding
    private val logger = appEnv.getLogger(DefaultSshGuestClient::class)
    private var _isInitialized = false

    override val isInitialized: Boolean get() = _isInitialized

    override suspend fun init(): AppResult<Unit> = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Initializing SshGuestClient" }
        initPortForwarding().bind()
        sshClient.start()
        session = sshClient.connect("intervirt", "127.0.0.1", fwd.externalPort)
            .verify()
            .session
        session.auth().verify()
        logger.info { "Initialized SSH connection with guest" }
        _isInitialized = true
    }

    override fun runCommand(vararg commands: String): AppResult<Flow<CommandStatus>> {
        val command = commands.joinToString(" ")
        logger.info { "Executing '$command' on guest" }
        return session.exec(command)
    }

    private suspend fun initPortForwarding(): AppResult<Unit> = either {
        val freePort = getFreePort()
        fwd = PortForwarding(
            protocol = "tcp",
            internalPort = 22,
            externalPort = freePort,
        )
        qemuClient.addPortForwarding(fwd).bind()
    }

    override suspend fun close(): AppResult<Unit> = either {
        session.close()
        sshClient.close()
        qemuClient.removePortForwarding(fwd).bind()
    }
}

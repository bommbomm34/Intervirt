/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.impl

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.api.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.api.SshGuestClient
import io.github.bommbomm34.intervirt.core.api.getFreePort
import io.github.bommbomm34.intervirt.core.api.getValue
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.ext.exec
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession

class DefaultSshGuestClient(
    private val qemuClient: QemuClient,
    envHolder: AppEnvHolder,
) : SshGuestClient {
    val appEnv by envHolder
    private val sshClient = SshClient.setUpDefaultClient()
    private lateinit var session: ClientSession
    private lateinit var fwd: PortForwarding
    private val logger = appEnv.getLogger(DefaultSshGuestClient::class)
    private var _isInitialized = false

    override val isInitialized: Boolean get() = _isInitialized

    context(_: Raise<Failure>)
    override suspend fun init() = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Initializing SshGuestClient" }
        initPortForwarding()
        sshClient.start()
        session = sshClient.connect("intervirt", "127.0.0.1", fwd.externalPort)
            .verify()
            .session
        session.auth().verify()
        logger.info { "Initialized SSH connection with guest" }
        _isInitialized = true
    }

    context(_: Raise<Failure>)
    override fun runCommand(vararg commands: String): Flow<CommandStatus> {
        val command = commands.joinToString(" ")
        logger.info { "Executing '$command' on guest" }
        return session.exec(command)
    }

    context(_: Raise<Failure>)
    private suspend fun initPortForwarding() {
        val freePort = getFreePort()
        fwd = PortForwarding(
            protocol = "tcp",
            internalPort = 22,
            externalPort = freePort,
        )
        qemuClient.addPortForwarding(fwd)
    }

    context(_: Raise<Failure>)
    override suspend fun close() {
        session.close()
        sshClient.close()
        qemuClient.removePortForwarding(fwd)
    }
}

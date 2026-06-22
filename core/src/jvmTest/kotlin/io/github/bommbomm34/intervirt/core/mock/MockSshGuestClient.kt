/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.mock

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import io.github.bommbomm34.intervirt.core.api.Executor
import io.github.bommbomm34.intervirt.core.api.SshGuestClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.bind
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.test.fails
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class MockSshGuestClient(appEnv: AppEnv) : SshGuestClient {
    private val executor = MockExecutor(appEnv)
    override val isInitialized = true

    context(_: Raise<Failure>)
    override suspend fun init() {
        ensure(!fails { exec("incus", "--help") }) { Failure.IllegalState("Incus is not installed!") }
    }

    context(_: Raise<Failure>)
    override fun runCommand(vararg commands: String): Flow<CommandStatus> = flow {
        emitAll(executor.runCommand(null, commands.toList()))
    }

    context(_: Raise<Failure>)
    override suspend fun close() {}

    context(_: Raise<Failure>)
    private suspend fun exec(vararg commands: String): String = executor.runCommand(null, commands.toList())
        .getCommandResult()
        .bind()
}

private class MockExecutor(appEnv: AppEnv) : Executor {
    private val logger = appEnv.getLogger(MockExecutor::class)
    override fun runCommand(
        workingFolder: PlatformFile?,
        commands: List<String>,
    ): Flow<CommandStatus> = flow {
        val args by lazy { commands.drop(1) }
        when (commands[0]) {
            "apk" -> apk(args)
            "incus" -> incus(args)
            else -> invalidCommand()
        }
    }

    private suspend fun FlowCollector<CommandStatus>.apk(args: List<String>) {
        when (args[0]) {
            "update" -> {
                emit("Updating package indices...".toCommandStatus())
                emit("Updated package indices".toCommandStatus())
                emit(0.toCommandStatus())
            }
            "upgrade" -> {
                emit("Upgrading SSH...".toCommandStatus())
                emit("Upgrading Intervirt Agent...".toCommandStatus())
                emit("Successfully upgraded".toCommandStatus())
                emit(0.toCommandStatus())
            }
            else -> invalidCommand()
        }
    }

    private suspend fun FlowCollector<CommandStatus>.incus(args: List<String>) {
        when (args[0]) {
            "config" -> {

            }
            "list" -> {

            }
            "network" -> {

            }
            "start" -> {

            }
            "stop" -> {

            }
        }
    }

    private suspend fun FlowCollector<CommandStatus>.invalidCommand() {
        emit("Invalid command".toCommandStatus())
        emit(1.toCommandStatus())
    }
}

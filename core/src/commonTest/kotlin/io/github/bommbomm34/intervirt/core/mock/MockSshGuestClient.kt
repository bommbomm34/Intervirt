/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.mock

import io.github.bommbomm34.intervirt.core.api.Executor
import io.github.bommbomm34.intervirt.core.api.SshGuestClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import io.github.bommbomm34.intervirt.core.util.ext.asSuccess
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.runSuspendingCatching
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class MockSshGuestClient(appEnv: AppEnv) : SshGuestClient {
    private val executor = MockExecutor(appEnv)
    override val isInitialized = true

    override suspend fun init(): Result<Unit> = runSuspendingCatching {
        check(exec("incus", "--help").isSuccess) { "Incus is not installed!" }
    }

    override fun runCommand(vararg commands: String): Result<Flow<CommandStatus>> = flow {
        emitAll(executor.runCommand(null, commands.toList()))
    }.asSuccess()

    override suspend fun close(): Result<Unit> = Result.success(Unit)

    private suspend fun exec(vararg commands: String): Result<String> = executor.runCommand(null, commands.toList())
        .getCommandResult()
        .asResult()
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
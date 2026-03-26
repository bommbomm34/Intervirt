/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import io.github.bommbomm34.intervirt.core.exceptions.CommandExecutionException
import kotlinx.coroutines.flow.Flow

sealed class CommandStatus {
    data class Running(val message: String) : CommandStatus()
    data class End(val statusCode: Int) : CommandStatus()
}

data class CommandResult(
    val output: String,
    val statusCode: Int,
) {
    fun asResult() = if (statusCode != 0) Result.failure(CommandExecutionException(statusCode, output)) else Result.success(output)
}

/**
 * Collects the flow and returns a `CommandResult`.
 * @throws NullPointerException if the flow doesn't contain a `CommandStatus.End`
 */
suspend fun Flow<CommandStatus>.getCommandResult(iterate: suspend (CommandStatus) -> Unit = {}): CommandResult {
    var statusCode: Int? = null
    val totalOutput = StringBuilder()
    collect {
        when (it) {
            is CommandStatus.Running -> {
                totalOutput.append(it.message + "\n")
                iterate(it)
            }

            is CommandStatus.End -> {
                statusCode = it.statusCode
                return@collect
            }
        }
    }
    return CommandResult(totalOutput.toString(), statusCode!!)
}

fun String.toCommandStatus() = CommandStatus.Running(this)
fun Int.toCommandStatus() = CommandStatus.End(this)
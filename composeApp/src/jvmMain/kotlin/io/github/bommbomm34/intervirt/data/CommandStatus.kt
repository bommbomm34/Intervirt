package io.github.bommbomm34.intervirt.data

import kotlinx.coroutines.flow.Flow

data class CommandStatus(
    val message: String? = null,
    val statusCode: Int? = null
)
suspend fun Flow<CommandStatus>.getTotalCommandStatus(iterate: suspend (CommandStatus) -> Unit = {}): CommandStatus {
    var statusCode: Int? = null
    val totalOutput = StringBuilder()
    collect {
        if (it.statusCode == null) {
            totalOutput.append(it.message)
            iterate(it)
        } else {
            statusCode = it.statusCode
            return@collect
        }
    }
    return CommandStatus(totalOutput.toString(), statusCode)
}

fun String.toCommandStatus() = CommandStatus(message = this)
fun Int.toCommandStatus() = CommandStatus(statusCode = this)
package io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl

import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.io.path.readText

sealed class VirtualContainer(val imageName: String) {
    val mapping = buildMapping { buildMapping() }

    open suspend fun start(ioClient: ContainerIOClient) {}

    open suspend fun stop(ioClient: ContainerIOClient) {}

    fun exec(commands: List<String>): Flow<CommandStatus> = mapping.exec(commands)

    protected open fun CommandMappingBuilder.buildMapping() {}

    data object Apache2 : VirtualContainer("apache2") {
        override fun CommandMappingBuilder.buildMapping() {
            onCommand("/usr/bin/a2ensite") { args ->
                if (args.size != 1) {
                    emitError("Expected exactly one argument")
                    return@onCommand
                }

                // no-op
                emitEnd(0)
            }
        }
    }

    data object CoreDns : VirtualContainer("coredns")
}

@JvmInline
value class CommandMapping(val map: MutableMap<String, (List<String>) -> Flow<CommandStatus>>)

class CommandMappingBuilder {
    val mappings: MutableMap<String, (List<String>) -> Flow<CommandStatus>> = mutableMapOf()

    fun build() = CommandMapping(mappings)
}

suspend fun FlowCollector<CommandStatus>.emitEnd(status: Int = 0) = emit(CommandStatus.End(status))

suspend fun FlowCollector<CommandStatus>.emitRunning(message: String) = emit(CommandStatus.Running(message))

suspend fun FlowCollector<CommandStatus>.emitError(message: String, status: Int = 1) {
    emitRunning(message)
    emitEnd(status)
}

private fun CommandMappingBuilder.onCommand(
    name: String,
    block: suspend FlowCollector<CommandStatus>.(List<String>) -> Unit,
) {
    mappings[name] = { flow { block(it) } }
}

private inline fun buildMapping(block: CommandMappingBuilder.() -> Unit): CommandMapping {
    return CommandMappingBuilder().apply(block).build()
}

private fun CommandMapping.exec(commands: List<String>): Flow<CommandStatus> {
    if (commands.isEmpty()) {
        return flow {
            emitRunning("Expected command, but got none")
            emitEnd(1)
        }
    }

    val handler = map[commands[0]] ?: return flow {
        emitRunning("Command not found: ${commands[0]}")
        emitEnd(1)
    }

    return handler(commands.drop(1))
}

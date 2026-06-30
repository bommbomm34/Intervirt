package io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.util.ext.flowCatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration.Companion.milliseconds

class VirtualDockerManager : DockerManager {
    private val containers = mutableListOf<Container>()

    context(_: Raise<Failure>)
    override suspend fun init() {}

    override fun addContainer(
        name: String,
        image: String,
        portForwardings: List<PortForwarding>,
        volumes: Map<String, String>,
        env: Map<String, String>,
        hostName: String?,
    ): Flow<ResultProgress<String>> = flowCatching {
        emit(ResultProgress.proceed(0f, "Starting adding container..."))
        delay(500.milliseconds)
        emit(ResultProgress.proceed(0.5f, "Pulling image..."))
        delay(1500.milliseconds)
        // name == id
        containers += Container(
            name = name,
            image = image,
            portForwardings = portForwardings.toMutableList(),
            volumes = volumes.toMutableMap(),
            env = env.toMutableMap(),
            hostName = hostName,
        )
        emit(ResultProgress.success(name))
    }

    context(_: Raise<Failure>)
    override suspend fun removeContainer(id: String) {
        containers.removeAll { it.name == id }
    }

    context(_: Raise<Failure>)
    override suspend fun startContainer(id: String) {
        container(id).running = true
    }

    context(_: Raise<Failure>)
    override suspend fun stopContainer(id: String) {
        container(id).running = false
    }

    context(_: Raise<Failure>)
    override suspend fun restartContainer(id: String) {
        container(id).running = false
        delay(1000.milliseconds)
        container(id).running = true
    }

    context(_: Raise<Failure>)
    override suspend fun getContainer(name: String): String? {
        if (containers.none { it.name == name }) return null

        return name
    }

    context(_: Raise<Failure>)
    override suspend fun isContainerRunning(id: String): Boolean {
        return container(id).running
    }

    context(_: Raise<Failure>)
    override suspend fun exec(
        id: String,
        commands: List<String>,
    ): Flow<CommandStatus> {
        if (commands.firstOrNull() == "echo") {
            val output = commands.drop(1).joinToString(" ")

            return flowOf(
                CommandStatus.Running(output),
                CommandStatus.End(0),
            )
        }

        return flowOf(
            CommandStatus.Running("This is just a mock"),
            CommandStatus.End(1),
        )
    }

    context(_: Raise<Failure>)
    override fun pullImage(image: String): Flow<ResultProgress<Unit>> {
        return flowOf(ResultProgress.success(Unit))
    }

    context(_: Raise<Failure>)
    override suspend fun checkHealth(id: String) {}

    context(_: Raise<Failure>)
    override suspend fun close() {}

    context(_: Raise<Failure>)
    private fun container(name: String) =
        containers.singleOrNull { it.name == name } ?: raise(Failure.NotFound("Container $name"))

    private data class Container(
        val name: String,
        val image: String,
        val portForwardings: MutableList<PortForwarding>,
        val volumes: MutableMap<String, String>,
        val env: MutableMap<String, String>,
        val hostName: String?,
        var running: Boolean = true,
    )
}

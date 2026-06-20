/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos.general


import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import kotlinx.coroutines.flow.Flow

interface DockerManager : AsyncCloseable {

    context(_: Raise<Failure>)
    suspend fun init()

    fun addContainer(
        name: String,
        image: String,
        portForwardings: List<PortForwarding> = emptyList(),
        volumes: Map<String, String> = emptyMap(),
        env: Map<String, String> = emptyMap(),
        hostName: String? = null,
    ): Flow<ResultProgress<String>>


    context(_: Raise<Failure>)
    suspend fun removeContainer(id: String)

    context(_: Raise<Failure>)
    suspend fun startContainer(id: String)

    context(_: Raise<Failure>)
    suspend fun stopContainer(id: String)

    context(_: Raise<Failure>)
    suspend fun restartContainer(id: String)

    context(_: Raise<Failure>)
    suspend fun getContainer(name: String): String?

    context(_: Raise<Failure>)
    suspend fun isContainerRunning(id: String): Boolean

    context(_: Raise<Failure>)
    suspend fun exec(id: String, commands: List<String>): Flow<CommandStatus>

    context(_: Raise<Failure>)
    fun pullImage(image: String): Flow<ResultProgress<Unit>>

    context(_: Raise<Failure>)
    suspend fun checkHealth(id: String)
}

/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import kotlinx.coroutines.flow.Flow

interface DockerManager : AsyncCloseable {

    suspend fun init(): Result<Unit>

    fun addContainer(
        name: String,
        image: String,
        portForwardings: List<PortForwarding> = emptyList(),
        volumes: Map<String, String> = emptyMap(),
        env: Map<String, String> = emptyMap(),
        hostName: String? = null,
    ): Flow<ResultProgress<String>>

    suspend fun removeContainer(id: String): Result<Unit>

    suspend fun startContainer(id: String): Result<Unit>

    suspend fun stopContainer(id: String): Result<Unit>

    suspend fun restartContainer(id: String): Result<Unit>

    suspend fun getContainer(name: String): Result<String?>

    suspend fun isContainerRunning(id: String): Result<Boolean>

    suspend fun exec(id: String, commands: List<String>): Result<Flow<CommandStatus>>

    fun pullImage(image: String): Flow<ResultProgress<Unit>>

    suspend fun checkHealth(id: String): Result<Unit>
}
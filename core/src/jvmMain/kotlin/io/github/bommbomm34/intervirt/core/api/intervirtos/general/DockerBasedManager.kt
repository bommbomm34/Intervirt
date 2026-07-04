/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import arrow.core.raise.context.Raise
import arrow.core.raise.context.bind
import io.github.bommbomm34.intervirt.core.api.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.getValue
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure

import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.ext.flowCatching
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.lastResult

import kotlinx.coroutines.flow.Flow
import kotlin.collections.mapKeys
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories

/**
 * `./` in `volumes` keys will be replaced with a default host path
 * for the container.
 */
abstract class DockerBasedManager(
    envHolder: AppEnvHolder,
    osClient: IntervirtOSClient,
    val containerName: String,
    val containerImage: String,
    val portForwardings: List<PortForwarding> = emptyList(),
    val volumes: Map<String, String> = emptyMap(),
    val env: Map<String, String> = emptyMap(),
    val hostName: String? = null,
) : AsyncCloseable {
    val appEnv by envHolder
    protected val client = osClient.getClient(this)
    private var internalId: String? = null
    protected val id: String
        get() {
            check(internalId != null) { "Manager of $containerName isn't successfully initialized" }
            return internalId!!
        }
    private val logger = appEnv.getLogger(DockerBasedManager::class)

    fun init(): Flow<ResultProgress<String>> = flowCatching {
            logger.debug { "Initializing manager of $containerName" }
            val potentialId = client.docker.getContainer(containerName)
            potentialId?.let {
                client.docker.startContainer(it)
                internalId = it
                emit(ResultProgress.success(it))
            }
            // Create new container
            val hostPath = client.ioClient.getPath("/opt/intervirt/$containerName/")
                .createDirectories()
                .absolutePathString()
            val newId = client.docker.addContainer(
                name = containerName,
                image = containerImage,
                portForwardings = portForwardings,
                volumes = volumes.mapKeys { it.key.replace("./", hostPath) },
                env = env,
                hostName = hostName,
            ).lastResult().bind()
            client.docker.startContainer(newId)
            internalId = newId
            emit(ResultProgress.success(newId))
    }

    context(_: Raise<Failure>)
    override suspend fun close() {}
}

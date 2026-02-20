package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.lastResult
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.withCatchingContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories

/**
 * `./` in `volumes` keys will be replaced with a default host path
 * for the container.
 */
abstract class DockerBasedManager(
    osClient: IntervirtOSClient,
    val containerName: String,
    val containerImage: String,
    val portForwardings: List<PortForwarding> = emptyList(),
    val volumes: Map<String, String> = emptyMap(),
    val env: Map<String, String> = emptyMap(),
    val hostName: String? = null,
) : AsyncCloseable {
    protected val client = osClient.getClient(this)
    private var internalId: String? = null
    protected val id: String
        get() {
            check(internalId != null) { "Manager of $containerName isn't successfully initialized" }
            return internalId!!
        }
    private val logger = KotlinLogging.logger {  }

    fun init(): Flow<ResultProgress<String>> = flow {
        withCatchingContext(Dispatchers.IO) {
            logger.debug { "Initializing manager of $containerName" }
            val potentialId = client.docker.getContainer(containerName).getOrThrow()
            potentialId?.let {
                client.docker.startContainer(it).getOrThrow()
                internalId = it
                return@withCatchingContext it
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
            ).lastResult().getOrThrow()
            client.docker.startContainer(newId).getOrThrow()
            internalId = newId
            newId
        }.onFailure { emit(ResultProgress.failure(it)) }
    }

    override suspend fun close(): Result<Unit> = Result.success(Unit)
}
package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.withCatchingContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories

abstract class DockerBasedManager(
    osClient: IntervirtOSClient,
    val containerName: String,
    val containerImage: String,
    val portForwardings: List<PortForwarding> = emptyList(),
    val bind: String? = null,
) : AsyncCloseable {
    protected val client = osClient.getClient(this)
    private var internalId: String? = null
    protected val id: String
        get() {
            check(internalId != null) { "Manager of $containerName isn't successfully initialized" }
            return internalId!!
        }
    private val logger = KotlinLogging.logger {  }

    suspend fun init(): Result<String> = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Initializing manager of $containerName" }
        val potentialId = client.docker.getContainer(containerName).getOrThrow()
        potentialId?.let {
            client.docker.startContainer(it).getOrThrow()
            internalId = it
            return@withCatchingContext it
        }
        // Create new container
        val hostPath = client.ioClient.getPath("/opt/intervirt/$containerName").createDirectories()
        val newId = client.docker.addContainer(
            name = containerName,
            image = containerImage,
            portForwardings = portForwardings,
            volumes = bind?.let { mapOf(hostPath.absolutePathString() to bind) } ?: emptyMap(),
        ).getOrThrow()
        client.docker.startContainer(newId).getOrThrow()
        internalId = newId
        newId
    }

    override suspend fun close(): Result<Unit> = Result.success(Unit)
}
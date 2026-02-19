package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.withCatchingContext
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
            require(internalId != null) { "Manager of $containerName isn't successfully initialized" }
            return internalId!!
        }

    suspend fun init(): Result<String> = withCatchingContext(Dispatchers.IO) {
        val potentialId = client.docker.getContainer(containerName).getOrThrow()
        potentialId?.let { return@withCatchingContext it }
        // Create new container
        val hostPath = client.ioClient.getPath("/opt/intervirt/$containerName").createDirectories()
        val newId = client.docker.addContainer(
            name = containerName,
            image = containerImage,
            portForwardings = portForwardings,
            volumes = bind?.let { mapOf(hostPath.absolutePathString() to bind) } ?: emptyMap(),
        ).getOrThrow()
        internalId = newId
        newId
    }

    override suspend fun close(): Result<Unit> = Result.success(Unit)
}
package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories

abstract class DockerBasedManager(
    private val osClient: IntervirtOSClient,
    private val containerName: String,
    private val containerImage: String,
    private val portForwardings: List<PortForwarding> = emptyList(),
    private val bind: String? = null,
) {
    private var internalId: String? = null
    protected val id: String
        get() {
            require(internalId != null) { "Manager of $containerName isn't successfully initialized" }
            return internalId!!
        }

    suspend fun init(): Result<String> = withCatchingContext(Dispatchers.IO) {
        val client = osClient.getClient()
        val potentialId = client.docker.getContainer(containerName).getOrThrow()
        potentialId?.let { return@withCatchingContext it }
        // Create new container
        val hostPath = client.ioClient.getPath("/opt/intervirt/$containerName")
            .createParentDirectories()
            .createDirectory()
        val newId = client.docker.addContainer(
            name = containerName,
            image = containerImage,
            portForwardings = portForwardings,
            volumes = bind?.let { mapOf(hostPath.absolutePathString() to bind) } ?: emptyMap(),
        ).getOrThrow()
        internalId = newId
        newId
    }
}
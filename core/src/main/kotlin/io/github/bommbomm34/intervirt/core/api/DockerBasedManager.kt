package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories

abstract class DockerBasedManager(
    private val docker: DockerManager,
    private val ioClient: ContainerIOClient,
    private val containerName: String,
    private val containerImage: String,
    private val portForwardings: List<PortForwarding> = emptyList(),
    private val volumes: Map<String, String> = emptyMap(),
) {
    private var internalId: String? = null
    protected val id: String
        get() {
            require(internalId != null) { "Manager of $containerName isn't successfully initialized" }
            return internalId!!
        }

    suspend fun init(): Result<String> = withCatchingContext(Dispatchers.IO) {
        val potentialId = docker.getContainer(containerName).getOrThrow()
        potentialId?.let { return@withCatchingContext it }
        // Create new container
        val hostPath = ioClient.getPath("/opt/intervirt/$containerName")
            .createParentDirectories()
            .createDirectory()
        val newId = docker.addContainer(
            name = containerName,
            image = containerImage,
            portForwardings = portForwardings,
            volumes = volumes,
        ).getOrThrow()
        internalId = newId
        newId
    }
}
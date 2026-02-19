package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.InspectContainerResponse
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.withCatchingContext
import kotlinx.coroutines.Dispatchers

class DockerManager(private val host: String) : AsyncCloseable {
    private var client: DockerClient? = null

    suspend fun init(): Result<Unit> = withCatchingContext(Dispatchers.IO){
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(host)
            .withDockerTlsVerify(false)
            .build()
        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .build()
        client = DockerClientImpl.getInstance(config, httpClient)
    }

    suspend fun addContainer(
        name: String,
        image: String,
        portForwardings: List<PortForwarding>,
        volumes: Map<String, String>,
    ): Result<String> = withCatchingContext(Dispatchers.IO) {
        val ports = portForwardings.map {
            val exposedPort = when (it.protocol) {
                "tcp" -> ExposedPort.tcp(it.guestPort)
                "udp" -> ExposedPort.udp(it.guestPort)
                else -> error("Invalid protocol ${it.protocol}")
            }
            val binding = Ports.Binding.bindPort(it.hostPort)
            val portBinding = PortBinding(binding, exposedPort)

            portBinding to exposedPort
        }
        val binds = volumes.map { Bind(it.key, Volume(it.value)) }
        val hostConfig = HostConfig.newHostConfig()
            .withPortBindings(ports.map { it.first })
            .withBinds(binds)

        getClient().createContainerCmd(image)
            .withName(name)
            .withHostConfig(hostConfig)
            .withExposedPorts(ports.map { it.second })
            .exec().id
    }

    suspend fun removeContainer(id: String): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        getClient().removeContainerCmd(id).exec()
    }

    suspend fun startContainer(id: String): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        getClient().startContainerCmd(id).exec()
    }

    suspend fun stopContainer(id: String): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        getClient().stopContainerCmd(id).exec()
    }

    suspend fun getContainer(name: String): Result<String?> = withCatchingContext(Dispatchers.IO) {
        val containers = getClient()
            .listContainersCmd()
            .withShowAll(true)
            .withNameFilter(listOf(name))
            .exec()
        containers.getOrNull(0)?.id
    }

    suspend fun isContainerRunning(id: String): Result<Boolean> = withCatchingContext(Dispatchers.IO) {
        val res = getClient()
            .inspectContainerCmd(id)
            .exec()
        res.state.running ?: false
    }

    override suspend fun close(): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        getClient().close()
    }

    private fun getClient(): DockerClient {
        val dockerClient = client
        require(dockerClient != null) { "Docker client is not successfully initialized" }
        return dockerClient
    }
}
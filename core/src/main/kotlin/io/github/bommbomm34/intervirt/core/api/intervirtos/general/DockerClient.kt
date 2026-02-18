package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DockerClient(
    host: String
) : AsyncCloseable {
    private val client: DockerClient

    init {
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
        image: String,
        name: String,
        portForwardings: List<PortForwarding>
    ): Result<String> = withCatchingContext(Dispatchers.IO){
//        val ports = portForwardings.associate {
//
//            to when (it.protocol){
//                "tcp" -> ExposedPort.tcp(it.guestPort)
//                "udp" -> ExposedPort.udp(it.guestPort)
//                else -> error("Invalid protocol ${it.protocol}")
//            }
//        }
//        val hostConfig = HostConfig.newHostConfig(portBindings).withPortBindings()
//
//        client.createContainerCmd(image)
//            .withName(name)
//            .withExposedPorts(exposedPorts)
//            .withHostConfig(hostConfig)
//            .exec().id
        // TODO: Handle port forwardings
        client.createContainerCmd(image)
            .withName(name)
            .exec().id
    }

    suspend fun removeContainer(id: String): Result<Unit> = withCatchingContext(Dispatchers.IO){
        client.removeContainerCmd(id).exec()
    }

    suspend fun startContainer(id: String): Result<Unit> = withCatchingContext(Dispatchers.IO){
        client.startContainerCmd(id).exec()
    }

    suspend fun stopContainer(id: String): Result<Unit> = withCatchingContext(Dispatchers.IO){
        client.stopContainerCmd(id).exec()
    }

    override suspend fun close(): Result<Unit> = withContext(Dispatchers.IO){
        runCatching {
            client.close()
        }
    }
}
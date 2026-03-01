package io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.exception.NotModifiedException
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.github.mwiede.dockerjava.jsch.JschDockerHttpClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import io.github.bommbomm34.intervirt.core.exceptions.UnhealthyDockerContainerException
import io.github.bommbomm34.intervirt.core.readablePercentage
import io.github.bommbomm34.intervirt.core.withCatchingContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.io.PipedInputStream
import java.io.PipedOutputStream

class ActualDockerManager(
    private val host: String,
) : DockerManager {
    private var client: DockerClient? = null
    private val logger = KotlinLogging.logger { }

    override suspend fun init(): Result<Unit> = catch {
        logger.debug { "Initializing ActualDockerManager with host $host" }
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(host)
            .withDockerTlsVerify(false)
            .build()
        val httpClient = when {
            host.startsWith("ssh://") -> {
                JschDockerHttpClient.Builder()
                    .dockerHost(config.dockerHost)
                    .build()
            }
            else -> {
                ApacheDockerHttpClient.Builder()
                    .dockerHost(config.dockerHost)
                    .build()
            }
        }
        client = DockerClientImpl.getInstance(config, httpClient)
    }

    override fun addContainer(
        name: String,
        image: String,
        portForwardings: List<PortForwarding>,
        volumes: Map<String, String>,
        env: Map<String, String>,
        hostName: String?,
    ): Flow<ResultProgress<String>> = flow {
        withCatchingContext(Dispatchers.IO) {
            pullImage(image).collect {
                when (it) {
                    is ResultProgress.Message<*> -> emit(ResultProgress.proceed(it.percentage * 0.9f, it.message))
                    is ResultProgress.Proceed<*> -> emit(ResultProgress.proceed(it.percentage * 0.9f))
                    is ResultProgress.Result<*> -> {} // Do nothing
                }
            }
            val ports = portForwardings.map {
                val exposedPort = when (it.protocol) {
                    "tcp" -> ExposedPort.tcp(it.internalPort)
                    "udp" -> ExposedPort.udp(it.internalPort)
                    else -> error("Invalid protocol ${it.protocol}")
                }
                val binding = Ports.Binding.bindPort(it.externalPort)
                val portBinding = PortBinding(binding, exposedPort)

                portBinding to exposedPort
            }
            val binds = volumes.map { Bind(it.key, Volume(it.value)) }
            val hostConfig = HostConfig.newHostConfig()
                .withPortBindings(ports.map { it.first })
                .withBinds(binds)
                .withRestartPolicy(RestartPolicy.unlessStoppedRestart())

            val cmd = getClient().createContainerCmd(image)
                .withName(name)
                .withHostConfig(hostConfig)
                .withExposedPorts(ports.map { it.second })
                .withEnv(env.map { "${it.key}=${it.value}" })

            emit(ResultProgress.success((if (hostName != null) cmd.withHostName(hostName) else cmd).exec().id))
        }.onFailure { emit(ResultProgress.failure(it)) }
    }

    override suspend fun removeContainer(id: String): Result<Unit> = catch {
        getClient().removeContainerCmd(id).exec()
    }

    override suspend fun startContainer(id: String): Result<Unit> = catch {
        getClient().startContainerCmd(id).exec()
    }

    override suspend fun stopContainer(id: String): Result<Unit> = catch {
        getClient().stopContainerCmd(id).exec()
    }

    override suspend fun restartContainer(id: String): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        getClient().restartContainerCmd(id).exec()
    }

    override suspend fun getContainer(name: String): Result<String?> = withCatchingContext(Dispatchers.IO) {
        val containers = getClient()
            .listContainersCmd()
            .withShowAll(true)
            .withNameFilter(listOf(name))
            .exec()
        containers.getOrNull(0)?.id
    }

    override suspend fun isContainerRunning(id: String): Result<Boolean> = withCatchingContext(Dispatchers.IO) {
        val res = getClient()
            .inspectContainerCmd(id)
            .exec()
        res.state.running ?: false
    }

    override suspend fun exec(id: String, commands: List<String>): Result<Flow<CommandStatus>> =
        withCatchingContext(Dispatchers.IO) {
            logger.debug { "Executing ${commands.joinToString(" ")} on container $id" }
            // Before performing any operations, check its health
            checkHealth(id).getOrThrow()
            val client = getClient()
            val exec = client
                .execCreateCmd(id)
                .withCmd(*commands.toTypedArray())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec()
            val output = PipedOutputStream()
            val reader = PipedInputStream(output).bufferedReader()
            val callback = object : ResultCallback.Adapter<Frame>() {
                override fun onNext(frame: Frame) {
                    output.write(frame.payload)
                    output.flush()
                }

                override fun onError(throwable: Throwable) = throw throwable // withCatchingContext will catch it

                override fun onComplete() = output.close()
            }
            client
                .execStartCmd(exec.id)
                .exec(callback)
            flow {
                reader.useLines { lines ->
                    lines.forEach {
                        emit(it.toCommandStatus())
                    }
                }
                val exitCode = client
                    .inspectExecCmd(exec.id)
                    .exec()
                    .exitCodeLong
                emit(exitCode.toInt().toCommandStatus())
            }
        }

    override fun pullImage(image: String): Flow<ResultProgress<Unit>> = flow {
        catch {
            val client = getClient()
            val callback = object : PullImageResultCallback() {
                override fun onStart(stream: Closeable) {
                    runBlocking {
                        emit(ResultProgress.proceed(0f, "Starting $image image pull"))
                    }
                }

                override fun onNext(item: PullResponseItem) {
                    val progress = item.progressDetail?.let { detail -> detail.total?.let { detail.current?.div(it) } }
                    val percentage = progress?.toFloat() ?: 0f
                    runBlocking {
                        emit(ResultProgress.proceed(percentage, "Pulling $image ${percentage.readablePercentage()}"))
                    }
                }

                override fun onError(throwable: Throwable) {
                    runBlocking {
                        emit(ResultProgress.failure(throwable))
                    }
                }

                override fun onComplete() {
                    runBlocking {
                        emit(ResultProgress.success(Unit))
                    }
                }
            }
            try {
                client.pullImageCmd(image)
                    .exec(callback)
                    .awaitCompletion()
            } catch (_: NotModifiedException) {
            } // Ignore it
        }.onFailure { emit(ResultProgress.failure(it)) }
    }

    override suspend fun checkHealth(id: String): Result<Unit> = catch {
        val res = getClient().inspectContainerCmd(id).exec()
        if (res.state.exitCodeLong != 0L) throw UnhealthyDockerContainerException(res.state.error ?: "Unknown error")
    }

    override suspend fun close(): Result<Unit> = catch {
        getClient().close()
    }

    private fun getClient(): DockerClient {
        val dockerClient = client
        require(dockerClient != null) { "Docker client is not successfully initialized" }
        return dockerClient
    }

    private suspend fun catch(
        block: suspend CoroutineScope.() -> Unit,
    ): Result<Unit> = withCatchingContext(Dispatchers.IO, block).recoverCatching {
        if (it is NotModifiedException) Unit else throw it
    }
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl

import arrow.core.raise.context.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.recover
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
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.exceptions.UnhealthyDockerContainerException
import io.github.bommbomm34.intervirt.core.util.ext.channelFlowCatching
import io.github.bommbomm34.intervirt.core.util.ext.flowCatching
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.readablePercentage
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import jdk.jfr.internal.OldObjectSample.emit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.io.PipedInputStream
import java.io.PipedOutputStream

class ActualDockerManager(
    appEnv: AppEnv,
    private val host: String,
) : DockerManager {
    private var client: DockerClient? = null
    private val logger = appEnv.getLogger(ActualDockerManager::class)

    context(_: Raise<Failure>)
    override suspend fun init() = catch {
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
    ): Flow<ResultProgress<String>> = flowCatching {
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
    }

    context(_: Raise<Failure>)
    override suspend fun removeContainer(id: String) = catch {
        getClient().removeContainerCmd(id).exec()
    }

    context(_: Raise<Failure>)
    override suspend fun startContainer(id: String) = catch {
        getClient().startContainerCmd(id).exec()
    }

    context(_: Raise<Failure>)
    override suspend fun stopContainer(id: String) = catch {
        getClient().stopContainerCmd(id).exec()
    }

    context(_: Raise<Failure>)
    override suspend fun restartContainer(id: String): Unit = withCatchingContext(Dispatchers.IO) {
        getClient().restartContainerCmd(id).exec()
    }

    context(_: Raise<Failure>)
    override suspend fun getContainer(name: String): String? = withCatchingContext(Dispatchers.IO) {
        val containers = getClient()
            .listContainersCmd()
            .withShowAll(true)
            .withNameFilter(listOf(name))
            .exec()
        containers.getOrNull(0)?.id
    }

    context(_: Raise<Failure>)
    override suspend fun isContainerRunning(id: String): Boolean = withCatchingContext(Dispatchers.IO) {
        val res = getClient()
            .inspectContainerCmd(id)
            .exec()
        res.state.running ?: false
    }

    context(_: Raise<Failure>)
    override suspend fun exec(id: String, commands: List<String>): Flow<CommandStatus> =
        withCatchingContext(Dispatchers.IO) {
            logger.debug { "Executing ${commands.joinToString(" ")} on container $id" }
            // Before performing any operations, check its health
            checkHealth(id)
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

    context(_: Raise<Failure>)
    override fun pullImage(image: String): Flow<ResultProgress<Unit>> = channelFlowCatching {
        val client = getClient()
        var failure: Failure? = null
        val callback = DefaultCallback(
            image = image,
            emit = { send(it) },
            onFailure = { failure = it },
        )
        try {
            client.pullImageCmd(image)
                .exec(callback)
                .awaitCompletion()
        } catch (_: NotModifiedException) {
            // no-op
        } finally {
            failure?.let { send(ResultProgress.failure(it)) }
        }
    }

    context(_: Raise<Failure>)
    override suspend fun checkHealth(id: String) = catch {
        val res = getClient().inspectContainerCmd(id).exec()
        if (res.state.exitCodeLong != 0L) throw UnhealthyDockerContainerException(res.state.error ?: "Unknown error")
    }

    context(_: Raise<Failure>)
    override suspend fun close() = catch {
        getClient().close()
    }

    private fun getClient(): DockerClient {
        val dockerClient = client
        require(dockerClient != null) { "Docker client is not successfully initialized" }
        return dockerClient
    }

    context(_: Raise<Failure>)
    private suspend fun catch(
        block: suspend context(Raise<Failure>) CoroutineScope.() -> Unit,
    ) = recover(
        block = {
            withCatchingContext(Dispatchers.IO) { block() }
        },
        recover = {
            if (it is Failure.Unexpected && it.exception is NotModifiedException) Unit else raise(it)
        },
    )

    private inner class DefaultCallback(
        private val image: String,
        private val emit: suspend (ResultProgress<Unit>) -> Unit,
        private val onFailure: (Failure) -> Unit,
    ) : PullImageResultCallback()
    {
        override fun onStart(stream: Closeable) {
            runBlocking {
                logger.info { "Starting $image image pull" }
                emit(ResultProgress.proceed(0f, "Starting $image image pull"))
            }
        }

        override fun onNext(item: PullResponseItem) {
            val progress = item.progressDetail?.let { detail -> detail.total?.let { detail.current?.div(it) } }
            val percentage = progress?.toFloat() ?: 0f
            runBlocking {
                logger.debug { "Pulling $image $percentage" }
                emit(ResultProgress.proceed(percentage, "Pulling $image ${percentage.readablePercentage()}"))
            }
        }

        override fun onError(throwable: Throwable) {
            runBlocking {
                logger.error { "Error occurred: $throwable" }
                // TODO: Improve error
                onFailure(Failure.Unexpected(throwable))
            }
        }

        override fun onComplete() {
            runBlocking {
                logger.debug { "Completed $image image pull" }
                emit(ResultProgress.success(Unit))
            }
        }
    }
}

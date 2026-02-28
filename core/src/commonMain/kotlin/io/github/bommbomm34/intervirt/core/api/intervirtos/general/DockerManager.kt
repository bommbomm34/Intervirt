package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.exception.NotModifiedException
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.mwiede.dockerjava.jsch.JschDockerHttpClient
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import io.github.bommbomm34.intervirt.core.exceptions.UnhealthyDockerContainerException
import io.github.bommbomm34.intervirt.core.readablePercentage
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
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

interface DockerManager : AsyncCloseable {

    suspend fun init(): Result<Unit>

    fun addContainer(
        name: String,
        image: String,
        portForwardings: List<PortForwarding> = emptyList(),
        volumes: Map<String, String> = emptyMap(),
        env: Map<String, String> = emptyMap(),
        hostName: String? = null,
    ): Flow<ResultProgress<String>>

    suspend fun removeContainer(id: String): Result<Unit>

    suspend fun startContainer(id: String): Result<Unit>

    suspend fun stopContainer(id: String): Result<Unit>

    suspend fun restartContainer(id: String): Result<Unit>

    suspend fun getContainer(name: String): Result<String?>

    suspend fun isContainerRunning(id: String): Result<Boolean>

    suspend fun exec(id: String, commands: List<String>): Result<Flow<CommandStatus>>

    fun pullImage(image: String): Flow<ResultProgress<Unit>>

    suspend fun checkHealth(id: String): Result<Unit>
}
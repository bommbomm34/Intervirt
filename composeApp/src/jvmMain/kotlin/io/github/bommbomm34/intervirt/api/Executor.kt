package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.CommandStatus
import io.github.bommbomm34.intervirt.data.Device
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class Executor(val deviceManager: DeviceManager) {
    private val logger = KotlinLogging.logger { }

    fun runCommandOnHost(workingFolder: File?, commands: List<String>): Flow<CommandStatus> =
        flow {
            val builder = ProcessBuilder(commands)
            workingFolder?.let { builder.directory(it) }
            builder.redirectErrorStream()
            logger.info { "Running '${commands.joinToString(" ")}' on host" }
            val process = builder.start()
            val reader = process.inputStream.bufferedReader()
            while (process.isAlive) {
                val line = reader.readLine() ?: continue
                emit(line.toCommandStatus())
            }
            emit(process.exitValue().toCommandStatus())
        }.flowOn(Dispatchers.IO)

    suspend fun runCommandOnGuest(computer: Device.Computer, commands: List<String>): Result<Flow<CommandStatus>> {
        return deviceManager.getSshClient(computer).map { sshClient ->
            flow {
                val command = commands.joinToString(" ")
                logger.info { "Running '$command' on container ${computer.id}" }
                sshClient.exec(command).use {
                    val reader = it.`in`.bufferedReader()
                    while (!it.isClosed) {
                        val line = reader.readLine() ?: continue
                        emit(line.toCommandStatus())
                    }
                    emit(it.exitStatus.toCommandStatus())
                }
            }
        }
    }

    private fun String.toCommandStatus() = CommandStatus(message = this)
    private fun Int.toCommandStatus() = CommandStatus(statusCode = this)
}
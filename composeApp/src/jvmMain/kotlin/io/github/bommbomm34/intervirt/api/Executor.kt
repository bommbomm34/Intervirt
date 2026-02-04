package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.CommandStatus
import io.github.bommbomm34.intervirt.data.toCommandStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class Executor {
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
                logger.debug { "Output: $line" }
                emit(line.toCommandStatus())
            }
            emit(process.exitValue().toCommandStatus())
        }.flowOn(Dispatchers.IO)
}
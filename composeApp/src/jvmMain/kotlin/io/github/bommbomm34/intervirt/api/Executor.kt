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
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    logger.debug { "Output: $line" }
                    emit(line.toCommandStatus())
                }
            }
            emit(process.waitFor().toCommandStatus())
        }.flowOn(Dispatchers.IO)
}
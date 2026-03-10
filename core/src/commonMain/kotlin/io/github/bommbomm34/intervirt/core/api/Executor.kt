/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import io.github.bommbomm34.intervirt.core.util.getLogger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

open class Executor(appEnv: AppEnv) {
    private val logger = appEnv.getLogger(Executor::class)

    open fun runCommandOnHost(workingFolder: File?, commands: List<String>): Flow<CommandStatus> =
        flow {
            require(workingFolder?.exists() ?: true) { "Working folder does not exist: ${workingFolder!!.absolutePath}" }
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
            val statusCode = process.waitFor()
            logger.debug { "Process ended with status code $statusCode" }
            emit(statusCode.toCommandStatus())
        }
            .flowOn(Dispatchers.IO)
            .catch {
                emit(it.localizedMessage.toCommandStatus())
                emit(1.toCommandStatus())
            }
}
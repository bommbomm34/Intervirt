/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util.ext

import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.sshd.client.session.ClientSession

fun ClientSession.exec(command: String): Result<Flow<CommandStatus>> = runCatching {
    val channel = createExecChannel(command)
    channel.open().verify()
    flow {
        val reader = channel.`in`.bufferedReader()
        while (!channel.isClosed) {
            val line = reader.readLine() ?: continue
            emit(line.toCommandStatus())
        }
        emit(channel.exitStatus.toCommandStatus())
    }.flowOn(Dispatchers.IO)
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util.ext

import arrow.core.raise.Raise
import arrow.core.raise.either

import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.sshd.client.session.ClientSession

context(_: Raise<Failure>)
fun ClientSession.exec(command: String): Flow<CommandStatus> {
    val channel = createExecChannel(command)
    channel.open().verify()
    return flow {
        val reader = channel.`in`.bufferedReader()
        while (!channel.isClosed) {
            val line = reader.readLine() ?: continue
            emit(line.toCommandStatus())
        }
        emit(channel.exitStatus.toCommandStatus())
    }.flowOn(Dispatchers.IO)
}

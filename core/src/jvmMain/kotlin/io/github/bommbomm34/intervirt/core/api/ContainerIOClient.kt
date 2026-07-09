/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.DeviceId
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

interface ContainerIOClient : AsyncCloseable {
    val id: DeviceId

    context(_: Raise<Failure>)
    fun exec(commands: List<String>): Flow<CommandStatus>

    fun getPath(path: String): Path
}

sealed class ShellControlMessage {
    class ByteData(val bytes: ByteArray) : ShellControlMessage()
    class Kill : ShellControlMessage()
    class End(val statusCode: Int) : ShellControlMessage()
    class Resize(val columns: Int, val rows: Int) : ShellControlMessage()
}

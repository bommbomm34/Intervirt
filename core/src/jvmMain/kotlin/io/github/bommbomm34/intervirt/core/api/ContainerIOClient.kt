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
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.file.Path

interface ContainerIOClient : AsyncCloseable {
    val id: DeviceId

    context(_: Raise<Failure>)
    fun exec(commands: List<String>): Flow<CommandStatus>

    fun getPath(path: String): Path
}

sealed class ShellControlMessage {
    class ByteData(val bytes: ByteArray) : ShellControlMessage()
    class End private constructor(val statusCode: Int) : ShellControlMessage() {
        companion object {
            private val SUCCESSFUL_END = End(0)

            fun of(statusCode: Int): End {
                if (statusCode == 0) return SUCCESSFUL_END

                return End(statusCode)
            }
        }
    }
    class Resize(val columns: Int, val rows: Int) : ShellControlMessage()
    object Kill : ShellControlMessage()

    companion object {
        fun end(statusCode: Int) = End.of(statusCode)
    }
}

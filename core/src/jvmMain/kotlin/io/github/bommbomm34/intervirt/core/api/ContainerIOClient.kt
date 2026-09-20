/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.api.ShellControlMessage.End
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

sealed interface ShellControlMessage {
    sealed interface Incoming : ShellControlMessage
    sealed interface Outgoing : ShellControlMessage

    data class Character(val char: Int) : Outgoing
    class Characters(val chars: CharArray) : Outgoing {
        override fun toString(): String = "Characters(chars=${chars.contentToString()})"
    }
    class Bytes(val bytes: ByteArray) : Incoming {
        override fun toString(): String = "Bytes(bytes=${bytes.contentToString()})"
    }
    class End private constructor(val statusCode: Int) : Outgoing {
        override fun toString(): String = "End(statusCode=$statusCode)"

        companion object {
            private val END_CACHE = Array(256, ::End)

            fun of(statusCode: Int): End = END_CACHE[statusCode and 0xFF]
        }
    }
    data class Resize(val columns: Int, val rows: Int) : Incoming
    data object Kill : Incoming

    companion object {
        fun end(statusCode: Int) = End.of(statusCode)
    }
}

/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api


import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import kotlinx.coroutines.flow.Flow

interface SshGuestClient : AsyncCloseable {
    val isInitialized: Boolean

    context(_: Raise<Failure>)
    suspend fun init()

    context(_: Raise<Failure>)
    fun runCommand(vararg commands: String): Flow<CommandStatus>
}

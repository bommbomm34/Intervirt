/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import kotlinx.coroutines.flow.Flow

interface SshGuestClient : AsyncCloseable {
    val isInitialized: Boolean

    suspend fun init(): Result<Unit>

    fun runCommand(vararg commands: String): Result<Flow<CommandStatus>>
}
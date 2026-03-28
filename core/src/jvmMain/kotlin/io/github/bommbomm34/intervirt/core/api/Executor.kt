/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

/**
 * An [Executor] can execute commands in a working folder asynchronously.
 */
interface Executor {
    fun runCommand(workingFolder: PlatformFile?, commands: List<String>): Flow<CommandStatus>
}
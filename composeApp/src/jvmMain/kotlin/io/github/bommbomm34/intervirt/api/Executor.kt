package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.api.impl.CommandStatus
import io.github.bommbomm34.intervirt.data.RemoteContainerSession
import kotlinx.coroutines.flow.Flow
import java.io.File

interface Executor {
    suspend fun getContainerSession(id: String): Result<RemoteContainerSession>

    fun runCommandOnHost(workingFolder: File? = null, commands: List<String>): Flow<CommandStatus>
}
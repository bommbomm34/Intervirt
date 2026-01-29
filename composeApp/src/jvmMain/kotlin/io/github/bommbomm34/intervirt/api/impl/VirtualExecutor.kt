package io.github.bommbomm34.intervirt.api.impl

import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.data.RemoteContainerSession
import kotlinx.coroutines.flow.Flow
import java.io.File

class VirtualExecutor : Executor {
    override suspend fun getContainerSession(id: String): Result<RemoteContainerSession> {
        TODO("Not yet implemented")
    }

    override fun runCommandOnHost(
        workingFolder: File?,
        commands: List<String>
    ): Flow<CommandStatus> {
        TODO("Not yet implemented")
    }
}
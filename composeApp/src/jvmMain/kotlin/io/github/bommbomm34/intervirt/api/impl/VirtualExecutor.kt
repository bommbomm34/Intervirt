package io.github.bommbomm34.intervirt.api.impl

import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.api.RemoteContainerSession
import io.github.oshai.kotlinlogging.KotlinLogging

class VirtualExecutor : Executor {
    override val logger = KotlinLogging.logger {  }
    private val sessions = mutableMapOf<String, VirtualRemoteContainerSession>()

    override suspend fun getContainerSession(id: String): Result<RemoteContainerSession> {
        val session = VirtualRemoteContainerSession(id)
        sessions[id] = session
        return Result.success(session)
    }
}
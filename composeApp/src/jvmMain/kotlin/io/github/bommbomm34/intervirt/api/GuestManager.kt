package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.ResultProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

interface GuestManager {
    suspend fun addContainer(
        id: String,
        initialIpv4: String,
        initialIpv6: String,
        mac: String,
        internet: Boolean,
        image: String
    ): Result<Unit>

    suspend fun removeContainer(id: String): Result<Unit>

    suspend fun setIpv4(id: String, newIP: String): Result<Unit>

    suspend fun setIpv6(id: String, newIP: String): Result<Unit>

    suspend fun connect(id1: String, id2: String): Result<Unit>

    suspend fun disconnect(id1: String, id2: String): Result<Unit>

    suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit>

    suspend fun addPortForwarding(id: String, internalPort: Int, externalPort: Int, protocol: String): Result<Unit>

    suspend fun removePortForwarding(externalPort: Int, protocol: String): Result<Unit>

    fun wipe(): Flow<ResultProgress<Unit>>

    fun update(): Flow<ResultProgress<Unit>>

    suspend fun shutdown(): Result<Unit>

    suspend fun reboot(): Result<Unit>

    suspend fun getVersion(): Result<String>
}
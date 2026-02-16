package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.defaultJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.path.writeText

/**
 * Saves data of IntervirtOS
 */
class IntervirtOSStore(ioClient: ContainerIOClient) {
    private val dataPath = ioClient.getPath("/opt/intervirt/data.json")
    private val data = mutableMapOf<String, String>()

    suspend fun saveString(key: String, value: String): Result<Unit> {
        data[key] = value
        return flush()
    }

    fun loadString(key: String) = data[key]

    suspend fun deleteString(key: String): Result<Unit> {
        data.remove(key)
        return flush()
    }

    private suspend fun flush() = withContext(Dispatchers.IO) {
        runCatching {
            dataPath.writeText(defaultJson.encodeToString(data))
        }
    }
}
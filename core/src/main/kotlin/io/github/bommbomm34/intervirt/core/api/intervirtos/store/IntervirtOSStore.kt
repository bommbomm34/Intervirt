package io.github.bommbomm34.intervirt.core.api.intervirtos.store

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

    suspend fun <T> set(accessor: Accessor<T>, value: T): Result<Unit> {
        data[accessor.name] = value.toString()
        return flush()
    }

    operator fun <T> get(accessor: Accessor<T>): T = accessor.produce(data[accessor.name])

    suspend fun <T> delete(accessor: Accessor<T>): Result<Unit> {
        data.remove(accessor.name)
        return flush()
    }

    private suspend fun flush() = withContext(Dispatchers.IO) {
        runCatching {
            dataPath.writeText(defaultJson.encodeToString(data))
        }
    }


    @Suppress("ClassName")
    sealed class Accessor<T>(val produce: (String?) -> T){
        val name = this::class.simpleName!!

        object MAIL_USERNAME : Accessor<String>({ it ?: "" })
        object MAIL_PASSWORD : Accessor<String>({ it ?: "" })
        object SMTP_SERVER_ADDRESS : Accessor<String>({ it ?: "" })
        object IMAP_SERVER_ADDRESS : Accessor<String>({ it ?: "" })
    }
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import arrow.core.raise.context.bind
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionSafety
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.parseAddress
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.path.*

/**
 * Saves data of IntervirtOS
 */
class IntervirtOSStore(
    appEnv: AppEnv,
    ioClient: ContainerIOClient,
) {
    private val logger = appEnv.getLogger(IntervirtOSStore::class, ioClient.id)
    private val dataPath = ioClient.getPath("/opt/intervirt/data.json")
    private val data = mutableMapOf<String, String>()

    suspend fun init(): AppResult<Unit> = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Initializing" }
        data.clear()
        if (dataPath.exists()) data.putAll(defaultJson.decodeFromString(dataPath.readText()))
        else {
            dataPath.createParentDirectories() // If missing
            dataPath.createFile()
            flush().bind()
        }
        logger.debug { "Initialized" }
    }

    suspend fun <T> set(accessor: Accessor<T>, value: T): AppResult<Unit> {
        logger.debug { "Setting ${accessor.name} to $value" }
        data[accessor.name] = value.toString()
        return flush()
    }

    operator fun <T> get(accessor: Accessor<T>): T = accessor.get(data[accessor.name])

    suspend fun <T> delete(accessor: Accessor<T>): AppResult<Unit> {
        logger.debug { "Deleting ${accessor.name}" }
        data.remove(accessor.name)
        return flush()
    }

    private suspend fun flush() = withCatchingContext(Dispatchers.IO) {
        dataPath.writeText(defaultJson.encodeToString(data))
    }


    @Suppress("ClassName")
    sealed class Accessor<T>(private val produce: (String?) -> T) {
        private var value: Any? = UNINITIALIZED
        val name = this::class.simpleName!!

        object MAIL_USERNAME : Accessor<String>({ it ?: "" })
        object SMTP_SERVER_ADDRESS : Accessor<Address>({ it?.parseAddress() ?: Address.EXAMPLE })
        object IMAP_SERVER_ADDRESS : Accessor<Address>({ it?.parseAddress() ?: Address.EXAMPLE })
        object SMTP_SAFETY : Accessor<MailConnectionSafety>(
            { str ->
                str?.let { MailConnectionSafety.valueOf(it) } ?: MailConnectionSafety.SECURE
            },
        )

        object IMAP_SAFETY : Accessor<MailConnectionSafety>(
            { str ->
                str?.let { MailConnectionSafety.valueOf(it) } ?: MailConnectionSafety.SECURE
            },
        )

        // General
        object HOSTNAME : Accessor<String?>({ it })

        private object UNINITIALIZED

        @Suppress("UNCHECKED_CAST")
        fun get(env: String?): T {
            if (value is UNINITIALIZED) {
                value = produce(env)
            }
            return value as T
        }
    }
}

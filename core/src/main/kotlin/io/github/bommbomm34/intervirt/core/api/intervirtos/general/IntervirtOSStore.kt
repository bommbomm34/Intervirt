package io.github.bommbomm34.intervirt.core.api.intervirtos.general

import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.SecretProvider
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionSafety
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.parseAddress
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.path.*

/**
 * Saves data of IntervirtOS
 */
class IntervirtOSStore(ioClient: ContainerIOClient) {
    private val logger = KotlinLogging.logger { }
    private val dataPath = ioClient.getPath("/opt/intervirt/data.json")
    private val data = mutableMapOf<String, String>()

    suspend fun init(): Result<Unit> = withContext(Dispatchers.IO) {
        runSuspendingCatching {
            logger.debug { "Initializing" }
            data.clear()
            if (dataPath.exists()) data.putAll(defaultJson.decodeFromString(dataPath.readText()))
            else {
                dataPath.createParentDirectories() // If missing
                dataPath.createFile()
                flush().getOrThrow()
            }
            SecretProvider.init().getOrThrow() // If not already done
        }
    }

    suspend fun <T> set(accessor: Accessor<T>, value: T): Result<Unit> {
        logger.debug { "Setting ${accessor.name} to $value" }
        val content = when (accessor) {
            is Accessor.Secure -> SecretProvider.encrypt(value as ByteArray)
            else -> value.toString()
        }
        data[accessor.name] = content
        return flush()
    }

    suspend operator fun get(accessor: Accessor.Secure): ByteArray =
        data[accessor.name]?.let { SecretProvider.decrypt(it) } ?: accessor.default

    operator fun <T> get(accessor: Accessor<T>): T = accessor.get(data[accessor.name])

    suspend fun <T> delete(accessor: Accessor<T>): Result<Unit> {
        logger.debug { "Deleting ${accessor.name}" }
        data.remove(accessor.name)
        return flush()
    }

    private suspend fun flush() = withContext(Dispatchers.IO) {
        runCatching {
            dataPath.writeText(defaultJson.encodeToString(data))
        }
    }


    @Suppress("ClassName")
    sealed class Accessor<T>(private val produce: (String?) -> T) {
        private var value: Any? = UNINITIALIZED
        val name = this::class.simpleName!!

        object MAIL_USERNAME : Accessor<String>({ it ?: "" })
        object MAIL_PASSWORD : Secure("".encodeToByteArray())
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

        // `produce` won't be called on this class
        abstract class Secure(val default: ByteArray = ByteArray(0)) : Accessor<ByteArray>({ ByteArray(0) })

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
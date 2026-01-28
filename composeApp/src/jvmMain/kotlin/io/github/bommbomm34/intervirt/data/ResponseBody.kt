package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.exceptions.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable()
sealed class ResponseBody {
    abstract val refID: String
    @SerialName("General")
    @Serializable
    data class General(
        override val refID: String,
        val error: String? = null,
        val code: Int = 0,
        val progress: Float? = null,
        val output: String? = null,
    ) : ResponseBody() {
        fun exception(): Exception? {
            return when (code) {
                1 -> UndefinedError(error!!)
                2 -> UnknownError()
                3 -> OperationAlreadyPerformedError()
                4 -> OSError(error!!)
                5 -> ContainerExecutionException(error!!)
                6 -> NotFoundError(error!!)
                7 -> NotSupportedOperationException()
                8 -> IllegalArgumentException(error!!)
                0 -> null
                else -> error("Invalid status code $code")
            }
        }
    }

    @SerialName("Version")
    @Serializable
    data class Version(
        override val refID: String,
        val version: String
    ) : ResponseBody()
}
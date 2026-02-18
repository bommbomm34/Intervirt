package io.github.bommbomm34.intervirt.core.data.agent

import io.github.bommbomm34.intervirt.core.exceptions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
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
                1 -> UndefinedException(error!!)
                2 -> UnknownException()
                3 -> OperationAlreadyPerformedException()
                4 -> OSException(error!!)
                5 -> ContainerExecutionException(error!!)
                6 -> NotFoundException(error!!)
                7 -> NotSupportedOperationException()
                8 -> IllegalArgumentException(error!!)
                // Error codes reserved internally for Intervirt Client
                100 -> AgentTimeoutException(refID)
                0 -> null
                else -> error("Invalid status code $code")
            }
        }
    }

    @SerialName("Version")
    @Serializable
    data class Version(
        override val refID: String,
        val version: String,
    ) : ResponseBody()


}
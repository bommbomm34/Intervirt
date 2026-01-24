package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.exceptions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ResponseBody(){
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
        val canRunCommands: Boolean
    ) : ResponseBody()
}


//    data class ContainerInfo (
//        val id: String,
//        val connected: List<String>,
//        val internet: Boolean,
//        val ipv4: String,
//        val ipv6: String,
//        val portForwardings: Map<Int, Int> // internalPort:externalPort
//    )

package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.exceptions.*
import kotlinx.serialization.Serializable

@Serializable
data class ResponseBody(
    val error: String? = null,
    val code: Int = 0,
    val progress: Float? = null,
    val output: String? = null
) {
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

@Serializable
data class VersionResponseBody(
    val version: String,
    val canRunCommands: Boolean
)

//    data class ContainerInfo (
//        val id: String,
//        val connected: List<String>,
//        val internet: Boolean,
//        val ipv4: String,
//        val ipv6: String,
//        val portForwardings: Map<Int, Int> // internalPort:externalPort
//    )

package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.exceptions.ContainerExecutionException
import io.github.bommbomm34.intervirt.exceptions.OSError
import io.github.bommbomm34.intervirt.exceptions.OperationAlreadyPerformedError
import io.github.bommbomm34.intervirt.exceptions.UndefinedError
import io.github.bommbomm34.intervirt.exceptions.UnknownError
import kotlinx.serialization.Serializable

@Serializable
data class ResponseBody (
    val error: String,
    val code: Int,
    val output: String? = null
){
    fun exception(): Exception {
        return when (code) {
            1 -> UndefinedError(error)
            2 -> UnknownError()
            3 -> OperationAlreadyPerformedError()
            4 -> OSError(error)
            5 -> ContainerExecutionException(error)
            0 -> error("No exception available for successful status code 0")
            else -> error("Invalid status code $code")
        }
    }
}

@Serializable
data class VersionResponseBody (
    val version: String
)

@Serializable
data class ContainerInfo (
    val id: String,
    val connected: List<String>,
    val internet: Boolean,
    val ipv4: String,
    val ipv6: String,
    val portForwardings: Map<Int, Int> // internalPort:externalPort
)
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data.agent

import io.github.bommbomm34.intervirt.core.data.Failure
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ResponseBody {
    abstract val refID: String

    open val end: Boolean get() = true
    open val success: Boolean get() = true

    @SerialName("General")
    @Serializable
    data class General(
        override val refID: String,
        val error: String? = null,
        val code: Int = 0,
        val progress: Float? = null,
        val output: String? = null,
        val status: Int = 0,
    ) : ResponseBody() {
        override val end get() = status >= 0
        override val success get() = code == 0

        fun failure(): Failure? {
            return when (code) {
                1 -> Failure.Undefined(error!!, refID)
                2 -> Failure.Unknown(refID)
                3 -> Failure.OperationAlreadyPerformed(error, refID)
                4 -> Failure.OS(error!!, refID)
                5 -> Failure.ContainerExecution(error!!, refID)
                6 -> Failure.NotFound(error!!, refID)
                7 -> Failure.NotSupportedOperation(refID)
                8 -> Failure.IllegalArgument(error!!)
                // Error codes reserved internally for Intervirt Client
                100 -> Failure.AgentTimeout(refID)
                0 -> null
                -1 -> error("The request isn't final yet: $this")
                else -> error("Invalid status code $code")
            }
        }
    }

    @SerialName("Info")
    @Serializable
    data class Info(
        override val refID: String,
        val version: String,
        @SerialName("ipv4_subnet")
        val ipv4Subnet: String,
        @SerialName("ipv6_subnet")
        val ipv6Subnet: String,
    ) : ResponseBody()

    @SerialName("ContainerList")
    @Serializable
    data class ContainerList(
        override val refID: String,
        val containers: List<ContainerInfo>,
    ) : ResponseBody()

    @SerialName("NetworkList")
    @Serializable
    data class NetworkList(
        override val refID: String,
        val networks: Map<String, List<String>>,
    ) : ResponseBody()
}

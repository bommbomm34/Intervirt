/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data.agent

import io.github.bommbomm34.intervirt.core.exceptions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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

        fun exception(): Exception? {
            return when (code) {
                1 -> UndefinedException(error!!, refID)
                2 -> UnknownException(refID)
                3 -> OperationAlreadyPerformedException(error, refID)
                4 -> OSException(error!!, refID)
                5 -> ContainerExecutionException(error!!, refID)
                6 -> NotFoundException(error!!, refID)
                7 -> NotSupportedOperationException(refID)
                8 -> IllegalArgumentException(error!!)
                // Error codes reserved internally for Intervirt Client
                100 -> AgentTimeoutException(refID)
                0 -> null
                -1 -> error("The request isn't final yet: $this")
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
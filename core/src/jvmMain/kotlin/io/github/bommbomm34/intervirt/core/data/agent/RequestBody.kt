/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data.agent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed class RequestBody {
    val uuid = UUID.randomUUID().toString()

    @SerialName("AddContainer")
    @Serializable
    data class AddContainer(
        val id: String,
        val ipv4: String,
        val ipv6: String,
        val mac: String,
        val internet: Boolean,
        val image: String,
    ) : RequestBody()

    @SerialName("IDWithNewIpv4")
    @Serializable
    data class IDWithNewIpv4(
        val id: String,
        val newIpv4: String,
    ) : RequestBody()

    @SerialName("IDWithNewIpv6")
    @Serializable
    data class IDWithNewIpv6(
        val id: String,
        val newIpv6: String,
    ) : RequestBody()

    @SerialName("Connect")
    @Serializable
    data class Connect(
        val container: String,
        val network: String,
    ) : RequestBody()

    @SerialName("Disconnect")
    @Serializable
    data class Disconnect(
        val container: String,
        val network: String,
    ) : RequestBody()

    @SerialName("SetInternetAccess")
    @Serializable
    data class SetInternetAccess(
        val id: String,
        val enabled: Boolean,
    ) : RequestBody()

    @SerialName("AddPortForwarding")
    @Serializable
    data class AddPortForwarding(
        val id: String,
        val internalPort: Int,
        val externalPort: Int,
        val protocol: String,
    ) : RequestBody()

    @SerialName("RemovePortForwarding")
    @Serializable
    data class RemovePortForwarding(
        val externalPort: Int,
        val protocol: String,
    ) : RequestBody()

    @SerialName("Command")
    @Serializable
    data class Command(
        val command: String,
    ) : RequestBody()

    @SerialName("RemoveContainer")
    @Serializable
    data class RemoveContainer(
        val id: String,
    ) : RequestBody()

    @SerialName("StartContainer")
    @Serializable
    data class StartContainer(
        val id: String,
    ) : RequestBody()

    @SerialName("StopContainer")
    @Serializable
    data class StopContainer(
        val id: String,
    ) : RequestBody()

    @SerialName("AddNetwork")
    @Serializable
    data class AddNetwork(
        val name: String,
    ) : RequestBody()

    @SerialName("RemoveNetwork")
    @Serializable
    data class RemoveNetwork(
        val name: String,
    ) : RequestBody()
}

fun String.commandBody() = RequestBody.Command(this)
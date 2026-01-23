package io.github.bommbomm34.intervirt.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed class RequestBody {
    val uuid = UUID.randomUUID().toString()

    @Serializable
    data class AddContainer (
        val id: String,
        val ipv4: String,
        val ipv6: String,
        val mac: String,
        val internet: Boolean,
        val image: String
    ) : RequestBody()

    @Serializable
    data class IDWithNewIpv4 (
        val id: String,
        val newIpv4: String,
    ) : RequestBody()

    @Serializable
    data class IDWithNewIpv6 (
        val id: String,
        val newIpv6: String,
    ) : RequestBody()

    @Serializable
    data class Connect (
        val id1: String,
        val id2: String
    ) : RequestBody()

    @Serializable
    data class Disconnect (
        val id1: String,
        val id2: String
    ) : RequestBody()

    @Serializable
    data class SetInternetAccess (
        val id: String,
        val enabled: Boolean
    ) : RequestBody()

    @Serializable
    data class AddPortForwarding (
        val id: String,
        val internalPort: Int,
        val externalPort: Int
    ) : RequestBody()

    @Serializable
    data class RemovePortForwarding (
        val externalPort: Int
    ) : RequestBody()

    @Serializable
    data class Command(
        val command: String
    ) : RequestBody()

    @Serializable
    data class RunCommand(
        val id: String,
        val command: String
    ) : RequestBody()

    @Serializable
    data class RemoveContainer (
        val id: String
    ) : RequestBody()
}

fun String.commandBody() = RequestBody.Command(this)
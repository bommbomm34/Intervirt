package io.github.bommbomm34.intervirt.data

import kotlinx.serialization.Serializable

@Serializable
sealed class RequestBody {
    abstract val command: String
    @Serializable
    data class AddContainer (
        val id: String,
        val ipv4: String,
        val ipv6: String,
        val mac: String,
        val internet: Boolean,
        val image: String,
        override val command: String = "addContainer",
    ) : RequestBody()

    @Serializable
    data class IdWithNewIp (
        val id: String,
        val newIP: String,
        override val command: String,
    ) : RequestBody()

    @Serializable
    data class Connect (
        val id1: String,
        val id2: String,
        override val command: String,
    ) : RequestBody()

    @Serializable
    data class SetInternetAccess (
        val id: String,
        val enabled: Boolean,
        override val command: String = "setInternetAccess",
    ) : RequestBody()

    @Serializable
    data class AddPortForwarding (
        val id: String,
        val internalPort: Int,
        val externalPort: Int,
        override val command: String = "addPortForwarding",
    ) : RequestBody()

    @Serializable
    data class RemovePortForwarding (
        val externalPort: Int,
        override val command: String = "removePortForwarding"
    ) : RequestBody()

    @Serializable
    data class ID(
        val id: String,
        override val command: String,
    ) : RequestBody()

    @Serializable
    data class Command(
        override val command: String
    ) : RequestBody()

    @Serializable
    data class RunCommand(
        val id: String,
        val shellCommand: String,
        val stateless: Boolean,
        override val command: String = "runCommand"
    ) : RequestBody()
}


fun String.idBody(command: String) = RequestBody.ID(command, this)
fun String.commandBody() = RequestBody.Command(this)
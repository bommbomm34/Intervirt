package io.github.bommbomm34.intervirt.data

import kotlinx.serialization.Serializable

@Serializable
data class AddContainerBody (
    val id: String,
    val initialIPv4: String,
    val initialIPv6: String,
    val internet: Boolean
)

@Serializable
data class IDWithNewIPBody (
    val id: String,
    val newIP: String
)

@Serializable
data class ConnectBody (
    val id1: String,
    val id2: String
)

@Serializable
data class SetInternetAccessBody (
    val id: String,
    val enabled: Boolean
)

@Serializable
data class ForwardPortBody (
    val id: String,
    val internalPort: Int,
    val externalPort: Int
)

@Serializable
data class IDBody(
    val id: String
)

fun String.idBody() = IDBody(this)
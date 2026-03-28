/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data.incus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncusInstanceInfo(
    val config: IncusInstanceConfig,
    val name: String,
    val status: String,
    val state: IncusInstanceState,
)

@Serializable
data class IncusInstanceConfig(
    @SerialName("image.os")
    val os: String,
    @SerialName("image.release")
    val release: String,
)

@Serializable
data class IncusInstanceState(
    val network: Map<String, IncusNetworkState>,
)

@Serializable
data class IncusNetworkState(
    val addresses: List<IncusNetworkAddress>,
    val hwaddr: String,
)

@Serializable
data class IncusNetworkAddress(
    val family: String,
    val address: String,
)
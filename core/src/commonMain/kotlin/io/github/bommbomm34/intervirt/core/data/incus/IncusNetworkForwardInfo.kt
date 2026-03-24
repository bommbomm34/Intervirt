/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data.incus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncusNetworkForwardInfo(
    val ports: List<IncusNetworkPortInfo>
)

@Serializable
data class IncusNetworkPortInfo(
    val protocol: String,
    @SerialName("listen_port")
    val listenPort: String,
    @SerialName("target_port")
    val targetPort: String,
)
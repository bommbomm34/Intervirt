/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data.agent

import io.github.bommbomm34.intervirt.core.data.DeviceId
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import kotlinx.serialization.Serializable

@Serializable
data class ContainerInfo(
    val id: DeviceId,
    val ipv4: String,
    val ipv6: String,
    val mac: String,
    val internet: Boolean,
    val image: String,
    val portForwardings: List<PortForwarding> = listOf(),
    val running: Boolean = true,
)

typealias Network = List<DeviceId>

/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PortForwarding(
    val protocol: String,
    val externalPort: Int,
    val internalPort: Int,
    @Transient val hidden: Boolean = false,
) {
    companion object {
        val DEFAULT = PortForwarding(
            protocol = "tcp",
            externalPort = 8080,
            internalPort = 80,
        )
    }

    override fun toString() = "$protocol:$externalPort:$internalPort"
}

fun Collection<PortForwarding>.excludeHidden() = filter { !it.hidden }
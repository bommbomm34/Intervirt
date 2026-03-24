/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data.incus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncusNetwork(
    val name: String,
    @SerialName("used_by")
    val usedBy: List<String>,
)
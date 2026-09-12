/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.util.ext.toReadableImage
import org.jetbrains.compose.resources.DrawableResource

data class Image(
    val name: String,
    val tag: String,
    val description: String,
    val iconSource: String,
    val descriptionSource: String,
    val icon: DrawableResource,
) {
    val fullName = "$name/$tag"

    fun toReadableName() = fullName.toReadableImage() ?: name
}

fun Device.Computer.hasIntervirtOS() = image.substringBefore("/") == "intervirtos"

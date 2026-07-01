/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.raise
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.util.ext.toReadableImage
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

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

fun ViewDevice.Computer.hasIntervirtOS() = image.substringBefore("/") == "intervirtos"

/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.toReadableImage
import io.github.bommbomm34.intervirt.runSuspendingCatching
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.Path

@Serializable
data class Image(
    val name: String,
    val tag: String,
    val description: String,
    val icon: String,
    val iconSource: String,
    val descriptionSource: String,
) {
    val fullName = "$name/$tag"

    fun toReadableName() = fullName.toReadableImage() ?: name
}

suspend fun HttpClient.getImages(url: String): Result<List<Image>> = runSuspendingCatching {
    val text = if (url.startsWith("file:///")){
        Files.readString(Path.of(url.substringAfter("file:///")))
    } else get(url).bodyAsText()
    defaultJson.decodeFromString(text)
}

fun ViewDevice.Computer.hasIntervirtOS() = image.substringBefore("/") == "intervirtos"
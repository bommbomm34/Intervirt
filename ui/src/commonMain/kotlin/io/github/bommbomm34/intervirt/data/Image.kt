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
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

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

context(_: Raise<Failure>)
suspend fun HttpClient.getImages(url: String): List<Image> = withContext(Dispatchers.IO) {
    val text = catch(
        block = {
            if (url.startsWith("file:///")) {
                Path.of(url.substringAfter("file:///")).readText()
            } else get(url).bodyAsText()
        },
        catch = { raise(Failure.Unexpected(it)) },
    )
    defaultJson.decodeFromString(text)
}

fun ViewDevice.Computer.hasIntervirtOS() = image.substringBefore("/") == "intervirtos"

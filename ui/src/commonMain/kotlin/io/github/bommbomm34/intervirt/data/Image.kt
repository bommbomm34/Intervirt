package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.runSuspendingCatching
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.Path

private var images: List<Image>? = null

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

    fun toReadableName() = fullName.toReadableImage()
}

suspend fun HttpClient.getImages(url: String): Result<List<Image>> = runSuspendingCatching {
    if (images == null){
        val text = if (url.startsWith("file:///")){
            Files.readString(Path.of(url.substringAfter("file:///")))
        } else get(url).bodyAsText()
        images = defaultJson.decodeFromString(text)
    }
    images!!
}

fun String.toReadableImage() = when {
    startsWith("debian/") -> "Debian"
    startsWith("ubuntu/") -> "Ubuntu"
    startsWith("intervirtos/") -> "IntervirtOS"
    startsWith("almalinux/") -> "AlmaLinux"
    startsWith("alpine/") -> "Alpine Linux"
    startsWith("archlinux/") -> "Arch Linux"
    startsWith("centos/") -> "CentOS"
    startsWith("fedora/") -> "Fedora"
    startsWith("gentoo/") -> "Gentoo"
    startsWith("kali/") -> "Kali Linux"
    startsWith("mint/") -> "Linux Mint"
    startsWith("nixos/") -> "NixOS"
    startsWith("opensuse/") -> "openSUSE"
    startsWith("voidlinux/") -> "Void Linux"
    else -> substringBefore("/")
}

fun ViewDevice.Computer.hasIntervirtOS() = image.substringBefore("/") == "intervirtos"
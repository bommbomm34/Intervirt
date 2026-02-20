package io.github.bommbomm34.intervirt.data

import androidx.compose.foundation.Image
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.IMAGES_URL
import io.github.bommbomm34.intervirt.runSuspendingCatching
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.getString

@Serializable
data class Image(
    val name: String,
    val tag: String,
    val description: String,
    val icon: String,
    val iconSource: String,
    val descriptionSource: String = "Wikipedia",
) {
    val fullName = "$name/$tag"

    fun toReadableName() = fullName.toReadableImage()
}

suspend fun HttpClient.getImages(url: String): Result<List<Image>> = runSuspendingCatching {
    get(url).body<List<Image>>()
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
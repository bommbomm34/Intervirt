package io.github.bommbomm34.intervirt.data

import intervirt.ui.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.getString

data class Image(
    val name: String,
    val tag: String,
    val description: String,
    val icon: DrawableResource,
) {
    val fullName = "$name/$tag"

    fun toReadableName() = fullName.toReadableImage()

    companion object {
        suspend fun getImages() = listOf(
            Image("debian", "trixie", getString(Res.string.debian_description), Res.drawable.debian),
            Image("fedora", "43", getString(Res.string.fedora_description), Res.drawable.fedora),
            Image("archlinux", "current", getString(Res.string.archlinux_description), Res.drawable.archlinux),
            // Other images are coming soon!
        )
    }
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
package io.github.bommbomm34.intervirt.data

import intervirt.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.getString


data class Image(
    val name: String,
    val tag: String,
    val description: String,
    val icon: DrawableResource
) {
    fun fullName() = "$name/$tag"

    companion object {
        suspend fun getImages() = listOf(
            Image("debian", "trixie", getString(Res.string.debian_description), Res.drawable.debian),
            Image("fedora", "43", getString(Res.string.fedora_description), Res.drawable.fedora),
            Image("archlinux", "current", getString(Res.string.archlinux_description), Res.drawable.archlinux)
            // Other images are coming soon!
        )
    }
}
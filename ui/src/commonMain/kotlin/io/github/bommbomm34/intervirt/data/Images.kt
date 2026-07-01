package io.github.bommbomm34.intervirt.data

import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.archlinux
import intervirt.ui.generated.resources.debian
import intervirt.ui.generated.resources.fedora
import intervirt.ui.generated.resources.intervirtos

val IMAGES = listOf(
    Image(
        name = "intervirtos",
        tag = "current",
        description = "IntervirtOS is a user-friendly operating system optimized for Intervirt. It contains some useful networking tools and lots of client and server software for experimenting.",
        iconSource = "bommbomm34",
        descriptionSource = "bommbomm34",
        icon = Res.drawable.intervirtos,
    ),
    Image(
        name = "debian",
        tag = "trixie",
        description = "Debian is an operating system developed by the Debian project established by Ian Murdock in August 1993.",
        iconSource = "Debian Project",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.debian
    ),
    Image(
        name = "fedora",
        tag = "43",
        description = "Fedora Linux is a free and open-source Linux distribution developed by the Fedora Project. It was originally developed in 2003 as a continuation of the Red Hat Linux project.",
        iconSource = "Red Hat, Inc.",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.fedora
    ),
    Image(
        name = "archlinux",
        tag = "current",
        description = "Arch Linux is an open source, rolling release Linux distribution. Arch Linux is kept up-to-date by regularly updating the individual pieces of software that it comprises.",
        iconSource = "Judd Vinet, Aaron Griffin and Levente Polyák",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.archlinux
    )
)

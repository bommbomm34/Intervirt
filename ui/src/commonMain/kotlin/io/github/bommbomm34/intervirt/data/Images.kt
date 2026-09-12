package io.github.bommbomm34.intervirt.data

import intervirt.ui.generated.resources.*

object Images {
    val INTERVIRT_OS = Image(
        name = "intervirtos",
        tag = "current",
        description = "IntervirtOS is a user-friendly operating system optimized for Intervirt. It contains some useful networking tools and lots of client and server software for experimenting.",
        iconSource = "bommbomm34",
        descriptionSource = "bommbomm34",
        icon = Res.drawable.intervirtos,
    )

    val DEBIAN = Image(
        name = "debian",
        tag = "trixie",
        description = "Debian is an operating system developed by the Debian project established by Ian Murdock in August 1993.",
        iconSource = "Debian Project",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.debian,
    )

    val FEDORA = Image(
        name = "fedora",
        tag = "43",
        description = "Fedora Linux is a free and open-source Linux distribution developed by the Fedora Project. It was originally developed in 2003 as a continuation of the Red Hat Linux project.",
        iconSource = "Red Hat, Inc.",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.fedora,
    )

    val ARCH_LINUX = Image(
        name = "archlinux",
        tag = "current",
        description = "Arch Linux is an open source, rolling release Linux distribution. Arch Linux is kept up-to-date by regularly updating the individual pieces of software that it comprises.",
        iconSource = "Judd Vinet, Aaron Griffin and Levente Polyák",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.archlinux,
    )

    val UBUNTU = Image(
        name = "ubuntu",
        tag = "resolute",
        description = "Ubuntu is a Linux distribution based on Debian and composed primarily of free and open-source software.",
        iconSource = "Canonical Ltd.",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.ubuntu,
    )

    val ALPINE = Image(
        name = "alpine",
        tag = "3.24",
        description = "Alpine Linux is a Linux distribution \"designed for power users who appreciate security, simplicity and resource efficiency\".",
        iconSource = "Alpine Linux Development Team",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.alpine,
    )

    val CENT_OS = Image(
        name = "centos",
        tag = "10-Stream",
        description = "CentOS Stream is a community enterprise Linux distribution that exists as a midstream between the upstream development in Fedora Linux and the downstream development for Red Hat Enterprise Linux.",
        iconSource = "Alain Reguera Delgado",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.centos,
    )

    val ALMA_LINUX = Image(
        name = "almalinux",
        tag = "10",
        description = "AlmaLinux is a free and open source Linux distribution, a community-supported, production-grade enterprise operating system that is binary-compatible with Red Hat Enterprise Linux (RHEL).",
        iconSource = "AlmaLinux",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.almalinux,
    )

    val GENTOO = Image(
        name = "gentoo",
        tag = "current",
        description = "Gentoo Linux is a Linux distribution built using the Portage package management system.",
        iconSource = "Gentoo Linux",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.gentoo,
    )

    val KALI_LINUX = Image(
        name = "kali",
        tag = "current",
        description = "Kali Linux is a Linux distribution designed for digital forensics and penetration testing.",
        iconSource = "Abiss21",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.kali_linux,
    )

    val LINUX_MINT = Image(
        name = "mint",
        tag = "zena",
        description = "Linux Mint is a community-developed Linux distribution for x86-64 systems, based on Ubuntu.",
        iconSource = "Clement Lefebvre",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.linux_mint,
    )

    val NIX_OS = Image(
        name = "nixos",
        tag = "26.05",
        description = "NixOS is a Linux distribution built around the Nix package manager.",
        iconSource = "Tim Cuthbertson",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.nixos,
    )

    val OPENSUSE = Image(
        name = "opensuse",
        tag = "16.0",
        description = "openSUSE is a free and open-source Linux distribution developed by the openSUSE Project.",
        iconSource = "openSUSE",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.opensuse,
    )

    val VOID_LINUX = Image(
        name = "voidlinux",
        tag = "current",
        description = "Void Linux is an independent Linux distribution that uses the X Binary Package System (XBPS) package manager, which was designed and implemented from scratch, and the runit init system.",
        iconSource = "Juan RP (xtraeme)",
        descriptionSource = "Wikipedia",
        icon = Res.drawable.voidlinux,
    )


    val ALL_IMAGES = listOf(
        DEBIAN,
        UBUNTU,
        INTERVIRT_OS,
        ALMA_LINUX,
        ALPINE,
        ARCH_LINUX,
        CENT_OS,
        FEDORA,
        GENTOO,
        KALI_LINUX,
        LINUX_MINT,
        NIX_OS,
        OPENSUSE,
        VOID_LINUX,
    )
}

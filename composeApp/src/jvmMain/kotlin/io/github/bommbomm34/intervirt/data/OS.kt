package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.exceptions.UnsupportedOsException

enum class OS {
    WINDOWS, LINUX
}

fun getOS(): OS {
    val os = System.getProperty("os.name")
    val ref = os.lowercase()
    return when {
        ref.startsWith("windows") -> OS.WINDOWS
        ref.startsWith("linux") -> OS.LINUX
        else -> throw UnsupportedOsException()
    }
}
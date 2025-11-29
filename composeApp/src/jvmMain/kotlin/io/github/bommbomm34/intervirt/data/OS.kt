package io.github.bommbomm34.intervirt.data

enum class OS {
    WINDOWS, LINUX
}

enum class Arch {
    X86_64, ARM64
}

fun getOS(): OS? {
    val os = System.getProperty("os.name")
    val ref = os.lowercase()
    return when {
        ref.startsWith("windows") -> OS.WINDOWS
        ref.startsWith("linux") -> OS.LINUX
        else -> null // OS is not supported
    }
}
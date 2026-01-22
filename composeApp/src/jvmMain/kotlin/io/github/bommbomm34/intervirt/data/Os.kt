package io.github.bommbomm34.intervirt.data

enum class Os {
    WINDOWS, LINUX
}

fun getOs(): Os? {
    val os = System.getProperty("os.name")
    val ref = os.lowercase()
    return when {
        ref.startsWith("windows") -> Os.WINDOWS
        ref.startsWith("linux") -> Os.LINUX
        else -> null // OS is not supported
    }
}
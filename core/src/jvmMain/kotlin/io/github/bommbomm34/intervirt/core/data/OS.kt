/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

enum class OS(private val asString: String) {
    WINDOWS("Windows"),
    LINUX("Linux");

    override fun toString() = asString

    companion object {
        val CURRENT = System.getProperty("os.name").let {
            when {
                it.startsWith("windows", ignoreCase = true) -> WINDOWS
                it.startsWith("linux", ignoreCase = true) -> LINUX
                else -> throw UnsupportedOsException()
            }
        }
    }
}

class UnsupportedOsException : Throwable("Only Windows and Linux are supported, but got: ${System.getProperty("os.name")}")

fun getOS() = OS.CURRENT

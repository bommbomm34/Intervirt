/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import io.github.bommbomm34.intervirt.core.exceptions.UnsupportedOsException

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

fun getOS() = OS.CURRENT

/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

interface OutputStream {
    val colorSupported: Boolean

    fun println(line: String): Unit = println(line)
    fun printlnErr(line: String)
}

expect fun getDefaultStream(): OutputStream
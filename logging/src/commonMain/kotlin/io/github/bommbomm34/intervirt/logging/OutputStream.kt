/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

interface OutputStream {
    val colorSupported: Boolean

    fun println(line: String): Unit = kotlin.io.println(line)
    fun printlnErr(line: String)

    companion object : OutputStream by getDefaultStream()
}

fun OutputStream.println(line: Any) = println(line.toString())

fun OutputStream.printlnErr(line: Any) = println(line.toString())

expect fun getDefaultStream(): OutputStream

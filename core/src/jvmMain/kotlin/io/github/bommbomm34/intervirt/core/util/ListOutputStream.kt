/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import io.github.bommbomm34.intervirt.logging.OutputStream

class ListOutputStream : OutputStream {
    companion object {
        val DEFAULT = ListOutputStream()
    }

    private val stdout = mutableListOf<String>()
    private val stderr = mutableListOf<String>()
    private val lock = Any()

    override val colorSupported get() = false

    override fun println(line: String) = synchronized(lock) {
        stdout += line
    }

    override fun printlnErr(line: String) = synchronized(lock) {
        stderr += line
    }

    fun getStdout(): List<String> = synchronized(lock) {
        stdout.toList()
    }

    fun getStderr(): List<String> = synchronized(lock) {
        stderr.toList()
    }
}

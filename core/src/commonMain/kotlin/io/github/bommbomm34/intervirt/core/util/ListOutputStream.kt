/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import io.github.bommbomm34.intervirt.logging.OutputStream
import io.github.bommbomm34.intervirt.logging.getDefaultStream

class ListOutputStream : OutputStream {
    companion object {
        val DEFAULT = ListOutputStream()
    }

    private val default = getDefaultStream()
    private var readingLog by atomic(false)
    private val stdout = mutableListOf<String>()
    private val stderr = mutableListOf<String>()
    override val colorSupported = false

    override fun println(line: String) {
        if (!readingLog) stdout.add(line)
    }

    override fun printlnErr(line: String) {
        if (!readingLog) stderr.add(line)
    }

    fun getStdout(): List<String> {
        readingLog = true
        val stdout: List<String> = this.stdout
        readingLog = false
        return stdout
    }

    fun getStderr(): List<String> {
        readingLog = true
        val stderr: List<String> = this.stderr
        readingLog = false
        return stderr
    }
}
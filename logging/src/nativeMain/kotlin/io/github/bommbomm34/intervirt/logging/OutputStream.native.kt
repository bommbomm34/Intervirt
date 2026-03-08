/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fprintf
import platform.posix.stderr

@OptIn(ExperimentalForeignApi::class)
actual fun getDefaultStream() = object : OutputStream {
    override val colorSupported = true

    override fun printlnErr(line: String) {
        fprintf(stderr, "$line\n")
    }
}
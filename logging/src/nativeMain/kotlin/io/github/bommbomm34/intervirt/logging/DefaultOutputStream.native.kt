/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fprintf
import platform.posix.stderr

internal actual object DefaultOutputStream : OutputStream {
    actual override val colorSupported: Boolean
        get() = true

    actual override fun printlnErr(line: String) {
        @OptIn(ExperimentalForeignApi::class)
        fprintf(stderr, "$line\n")
    }
}

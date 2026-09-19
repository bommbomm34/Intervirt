/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

internal actual object DefaultOutputStream : OutputStream {
    actual override val colorSupported: Boolean
        get() = false

    actual override fun printlnErr(line: String) {
        console.error(line)
    }
}

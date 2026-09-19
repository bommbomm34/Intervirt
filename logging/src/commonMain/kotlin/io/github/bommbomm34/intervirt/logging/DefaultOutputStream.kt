/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

internal expect object DefaultOutputStream : OutputStream {
    override val colorSupported: Boolean
    override fun printlnErr(line: String)
}

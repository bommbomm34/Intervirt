/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getDefaultStream() = object : OutputStream {
    override val colorSupported = false

    override fun printlnErr(line: String) {
        js("console.error('$line')")
    }
}
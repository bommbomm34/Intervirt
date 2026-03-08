/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.bommbomm34.intervirt.logging

actual fun getDefaultStream() = object : OutputStream {
    override val colorSupported = false

    override fun printlnErr(line: String) {
        wasmPrintErr(line)
    }
}

@JsFun("(line) => console.error(line)")
private external fun wasmPrintErr(line: String)
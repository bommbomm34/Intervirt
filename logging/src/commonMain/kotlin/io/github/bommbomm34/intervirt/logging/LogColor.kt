/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

import kotlin.jvm.JvmInline

@PublishedApi
@JvmInline
internal value class LogColor private constructor(val value: String) {
    override fun toString(): String = value

    companion object {
        val RED = LogColor("\u001B[31m")
        val GREEN = LogColor("\u001B[32m")
        val YELLOW = LogColor("\u001B[33m")
        val BLUE = LogColor("\u001B[34m")
        val DEFAULT = LogColor("")
    }
}

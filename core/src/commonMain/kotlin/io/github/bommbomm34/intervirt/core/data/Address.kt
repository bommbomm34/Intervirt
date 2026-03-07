/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

data class Address(
    val host: String,
    val port: Int,
) {
    companion object {
        val EXAMPLE = Address("example.com", 1234)
    }

    override fun toString() = "$host:$port"
}

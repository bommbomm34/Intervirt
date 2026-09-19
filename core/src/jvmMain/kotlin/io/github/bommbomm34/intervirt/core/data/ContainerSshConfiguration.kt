/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

data class ContainerSshConfiguration(
    val host: String,
    val port: Int,
    val username: String,
    val password: String?,
) {
    companion object {
        private const val DEFAULT_HOST = "127.0.0.1"
        private const val DEFAULT_USERNAME = "root"

        fun default(port: Int) = ContainerSshConfiguration(
            host = DEFAULT_HOST,
            port = port,
            username = DEFAULT_USERNAME,
            password = null,
        )
    }
}

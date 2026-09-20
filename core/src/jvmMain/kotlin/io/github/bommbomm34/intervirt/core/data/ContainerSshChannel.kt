/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import io.github.bommbomm34.intervirt.core.api.ShellControlMessage
import kotlinx.coroutines.channels.Channel

data class ContainerSshChannel(
    private val isClosedSupplier: () -> Boolean,
    val incoming: Channel<ShellControlMessage.Incoming> = Channel(capacity = DEFAULT_CAPACITY),
    val outgoing: Channel<ShellControlMessage.Outgoing> = Channel(capacity = DEFAULT_CAPACITY),
) : AutoCloseable {
    val isClosed get() = isClosedSupplier()

    override fun close() {
        incoming.close()
        outgoing.close()
    }

    companion object {
        private const val DEFAULT_CAPACITY = 5
    }
}

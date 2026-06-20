/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.data.Failure


interface AsyncCloseable {
    context(_: Raise<Failure>)
    suspend fun close()
}

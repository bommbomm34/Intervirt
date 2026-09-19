/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import io.github.bommbomm34.intervirt.core.api.Downloader

class UpdaterState {
    val updates = mutableStateSetOf<Downloader.Component>()
    val applyUpdates = mutableStateListOf<Downloader.Component>()
}

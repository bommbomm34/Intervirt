/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.mutableStateListOf
import io.github.bommbomm34.intervirt.core.api.Downloader

class UpdaterState {
    val updates = mutableStateListOf<Downloader.Component>()
    val applyUpdates = mutableStateListOf<Downloader.Component>()
}
package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.mutableStateListOf
import io.github.bommbomm34.intervirt.core.api.Downloader

class UpdaterState {
    val updates = mutableStateListOf<Downloader.Component>()
    val applyUpdates = mutableStateListOf<Downloader.Component>()
}
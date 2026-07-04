/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.env.storeEnv
import io.github.bommbomm34.intervirt.data.AppState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    private val appState: AppState,
    private val settings: Settings,
) : ViewModel() {
    var appEnv by mutableStateOf(appState.env)
    val changed get() = appEnv != appState.env

    fun saveChanges() {
        settings.storeEnv(appEnv)
        appState.env = appEnv
    }
}

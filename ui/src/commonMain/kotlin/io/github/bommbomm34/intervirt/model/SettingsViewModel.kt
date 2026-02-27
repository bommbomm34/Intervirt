package io.github.bommbomm34.intervirt.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.AppState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    private val appState: AppState,
    private val baseAppEnv: AppEnv,
) : ViewModel() {
    var changed by mutableStateOf(false)
    val appEnv = baseAppEnv.copy(
        autoFlush = false,
        onChange = { changed = true },
    )

    fun saveChanges(){
        appEnv.flush()
        baseAppEnv.invalidateCache()
        appState.appEnvChangeKey++
    }
}
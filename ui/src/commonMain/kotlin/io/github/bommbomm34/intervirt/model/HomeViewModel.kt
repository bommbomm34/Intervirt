package io.github.bommbomm34.intervirt.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.bommbomm34.intervirt.core.roundBy
import io.github.bommbomm34.intervirt.data.AppState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    val appState: AppState
) : ViewModel() {
    var devicesViewRenderKey by mutableStateOf(0)
    var showOptions by mutableStateOf(false)

    fun onConfChange(){
        devicesViewRenderKey++
    }

    fun getZoom() = "${appState.devicesViewZoom.roundBy(1)}x"
}
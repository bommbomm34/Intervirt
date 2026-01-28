package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.gui.intervirtos.home.AppInfo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Main(computer: ViewDevice.Computer){
    val appState = koinInject<AppState>()
    var appInfo: AppInfo? by remember { mutableStateOf(null) }
    AnimatedVisibility(appInfo == null){
        Home {
            appInfo = it
        }
    }
    appState.osWindowTitle = appInfo?.name?.let { "IntervirtOS ${computer.name} - ${stringResource(it)}" }
    AnimatedVisibility(appInfo != null){
        AlignedBox(Alignment.TopStart){
            CloseButton { appInfo = null }
        }
        appInfo?.content(computer)
    }
}
package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Main(computer: ViewDevice.Computer) {
    val appState = koinInject<AppState>()
    val deviceManager = koinInject<DeviceManager>()
    var osClient: IntervirtOSClient? by remember { mutableStateOf(null) }
    var appInfo: io.github.bommbomm34.intervirt.intervirtos.home.AppInfo? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        appState.runDialogCatching {
            osClient = deviceManager.getIntervirtOSClient(computer.device).getOrThrow()
        }
    }
    osClient?.let { osClient ->
        AnimatedVisibility(appInfo == null) {
            Home {
                appInfo = it
            }
        }
        appState.osWindowTitle = appInfo?.name?.let { "IntervirtOS ${computer.name} - ${stringResource(it)}" }
        AnimatedVisibility(appInfo != null) {
            AlignedBox(Alignment.TopStart) {
                CloseButton { appInfo = null }
            }
            appInfo?.content(osClient)
        }
    }
}
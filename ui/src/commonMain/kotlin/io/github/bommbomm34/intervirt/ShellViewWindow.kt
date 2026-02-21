package io.github.bommbomm34.intervirt

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.ShellView
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import org.koin.compose.koinInject

@Composable
fun ShellViewWindow(computer: ViewDevice.Computer) {
    val deviceManager = koinInject<DeviceManager>()
    val appState = koinInject<AppState>()
    var ioClient: ContainerIOClient? by remember { mutableStateOf(null) }
    LaunchedEffect(computer) {
        appState.runDialogCatching {
            ioClient = deviceManager.getIOClient(computer.device).getOrThrow()
        }
    }
    CenterColumn {
        ioClient?.let {
            ShellView(it)
        }
    }
}
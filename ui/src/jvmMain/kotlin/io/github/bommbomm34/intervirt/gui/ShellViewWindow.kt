package io.github.bommbomm34.intervirt.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.ShellView
import org.koin.compose.koinInject

@Composable
fun ShellViewWindow(computer: ViewDevice.Computer){
    val deviceManager = koinInject<DeviceManager>()
    val appState = koinInject<AppState>()
    var ioClient: ContainerIOClient? by remember { mutableStateOf(null) }
    LaunchedEffect(computer){
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
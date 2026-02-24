package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.bommbomm34.intervirt.components.buttons.PlayButton
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import org.koin.compose.koinInject

@Composable
fun ComputerStartButton(device: ViewDevice.Computer) {
    val appState = koinInject<AppState>()
    val deviceManager = koinInject<DeviceManager>()
    val scope = rememberCoroutineScope()
    PlayButton(device.running) {
        scope.launchDialogCatching(appState) {
            if (it) {
                // Start
                deviceManager.start(device.device).getOrThrow()
            } else {
                // Stop
                deviceManager.stop(device.device).getOrThrow()
            }
            device.running = it
        }
    }
}
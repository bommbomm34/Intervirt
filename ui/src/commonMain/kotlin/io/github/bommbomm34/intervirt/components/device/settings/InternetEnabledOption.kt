package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.internet_access
import io.github.bommbomm34.intervirt.components.NamedCheckbox
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun InternetEnabledOption(device: ViewDevice.Computer) {
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    val appState = koinInject<AppState>()
    NamedCheckbox(
        checked = device.internetEnabled,
        onCheckedChange = {
            scope.launchDialogCatching(appState) {
                device.internetEnabled = it
                deviceManager.setInternetEnabled(device.device, it).getOrThrow()
            }
        },
        name = stringResource(Res.string.internet_access),
    )
}
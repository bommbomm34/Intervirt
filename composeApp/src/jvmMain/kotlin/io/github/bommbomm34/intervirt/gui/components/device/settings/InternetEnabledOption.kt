package io.github.bommbomm34.intervirt.gui.components.device.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.internet_access
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun InternetEnabledOption(device: ViewDevice.Computer){
    val scope = rememberCoroutineScope()
    val deviceManager = koinInject<DeviceManager>()
    NamedCheckbox(
        checked = device.internetEnabled,
        onCheckedChange = {
            scope.launch {
                device.internetEnabled = it
                deviceManager.setInternetEnabled(device.toDevice(), it)
            }
        },
        name = stringResource(Res.string.internet_access)
    )
}
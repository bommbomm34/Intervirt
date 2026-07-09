/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.ShellView
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.data.Device
import org.koin.compose.koinInject

@Composable
fun ShellViewWindow(computer: Device.Computer) {
    val deviceManager = koinInject<DeviceManager>()
    var ioClient: ContainerIOClient? by remember { mutableStateOf(null) }
    CatchingLaunchedEffect(computer) {
        ioClient = deviceManager.getIOClient(computer)
    }
    CenterColumn {
        ioClient?.let {
            ShellView(it)
        }
    }
}

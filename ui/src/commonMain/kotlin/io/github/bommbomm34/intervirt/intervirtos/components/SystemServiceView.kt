/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos.components

import androidx.compose.runtime.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.start
import intervirt.ui.generated.resources.stop
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.TooltipArea
import io.github.bommbomm34.intervirt.components.buttons.PlayButton
import io.github.bommbomm34.intervirt.core.api.SystemServiceManager
import io.github.bommbomm34.intervirt.intervirtos.model.components.SystemServiceViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SystemServiceView(
    serviceName: String,
    serviceManager: SystemServiceManager,
) {
    val viewModel = koinViewModel<SystemServiceViewModel> { parametersOf(serviceName, serviceManager) }
    var running by remember { mutableStateOf(false) }
    CatchingLaunchedEffect(serviceManager, serviceName) {
        running = serviceManager.status(serviceName).active
    }
    TooltipArea(if (running) Res.string.stop else Res.string.start) {
        PlayButton(
            playing = running,
            onClick = viewModel::enable,
        )
    }
}

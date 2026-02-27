package io.github.bommbomm34.intervirt.intervirtos.components

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
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
    CatchingLaunchedEffect {
        running = serviceManager.status(serviceName).getOrThrow().active
    }
    PlayButton(
        playing = running,
        onClick = viewModel::enable,
    )
}
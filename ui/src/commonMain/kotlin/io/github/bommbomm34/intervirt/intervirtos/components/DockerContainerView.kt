package io.github.bommbomm34.intervirt.intervirtos.components

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.components.buttons.PlayButton
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerManager
import io.github.bommbomm34.intervirt.intervirtos.model.components.DockerContainerViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DockerContainerView(
    name: String,
    dockerManager: DockerManager,
) {
    val viewModel = koinViewModel<DockerContainerViewModel> { parametersOf(name, dockerManager) }
    viewModel.id?.let {
        PlayButton(
            playing = viewModel.running,
            onClick = viewModel::enable,
        )
    }
}
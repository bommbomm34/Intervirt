/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos.components

import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.start
import intervirt.ui.generated.resources.stop
import io.github.bommbomm34.intervirt.components.TooltipArea
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
        TooltipArea(if (viewModel.running) Res.string.stop else Res.string.start) {
            PlayButton(
                playing = viewModel.running,
                onClick = viewModel::enable,
            )
        }
    }
}

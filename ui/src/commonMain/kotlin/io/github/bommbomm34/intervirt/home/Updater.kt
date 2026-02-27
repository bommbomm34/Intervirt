package io.github.bommbomm34.intervirt.home

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.update
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.NamedCheckbox
import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.readablePercentage
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.model.home.UpdaterViewModel
import io.github.bommbomm34.intervirt.rememberLogger
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun Updater(onClose: () -> Unit) {
    val viewModel = koinViewModel<UpdaterViewModel> { parametersOf(onClose) }
    CenterColumn {
        viewModel.updates.forEach { component ->
            NamedCheckbox(
                checked = viewModel.applyUpdates.contains(component),
                onCheckedChange = {
                    if (it) viewModel.applyUpdates.add(component) else viewModel.applyUpdates.remove(component)
                },
                name = component.readableName,
            )
            GeneralSpacer()
        }
    }
    GeneralSpacer()
    // Update button
    Button(
        onClick = viewModel::update,
    ) {
        Text(stringResource(Res.string.update))
    }
}
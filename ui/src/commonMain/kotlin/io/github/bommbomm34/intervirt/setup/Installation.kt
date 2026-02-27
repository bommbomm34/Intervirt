package io.github.bommbomm34.intervirt.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.FlowProgressView
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.NamedCheckbox
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.model.setup.InstallationViewModel
import io.github.bommbomm34.intervirt.rememberLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Installation() {
    val viewModel = koinViewModel<InstallationViewModel>()
    val appState = koinInject<AppState>()
    CenterColumn {
        NamedCheckbox(
            checked = viewModel.allowInstallation,
            onCheckedChange = { viewModel.allowInstallation = it },
            name = stringResource(Res.string.allow_to_install_intervirt),
        )
        GeneralSpacer(8.dp)
        Button(
            onClick = viewModel::clickButton,
            enabled = viewModel.allowInstallation,
            colors = if (viewModel.job != null) ButtonDefaults.buttonColors(containerColor = Color.Red) else ButtonDefaults.buttonColors(),
        ) {
            if (viewModel.job != null) Text(
                text = stringResource(Res.string.cancel_intervirt_installation),
                color = Color.White,
            ) else Text(
                text = stringResource(Res.string.install_intervirt),
            )
        }
        AnimatedVisibility(
            visible = viewModel.flow != null,
        ) {
            GeneralSpacer(8.dp)
            FlowProgressView(
                flow = viewModel.flow ?: flowOf(),
                onJobChange = { viewModel.job = it },
            )
        }
        GeneralSpacer(8.dp)
        NamedCheckbox(
            checked = appState.showLogs,
            onCheckedChange = { appState.showLogs = it },
            name = stringResource(Res.string.show_logs),
        )
    }
}
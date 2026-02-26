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
import io.github.bommbomm34.intervirt.rememberLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Installation() {
    val logger = rememberLogger("Installation")
    val downloader = koinInject<Downloader>()
    val appEnv = koinInject<AppEnv>()
    val fileManager = koinInject<FileManager>()
    val appState = koinInject<AppState>()
    var allowInstallation by remember { mutableStateOf(false) }
    var flow: Flow<ResultProgress<String>>? by remember { mutableStateOf(null) }
    var job: Job? by remember { mutableStateOf(null) }
    CenterColumn {
        NamedCheckbox(
            checked = allowInstallation,
            onCheckedChange = { allowInstallation = it },
            name = stringResource(Res.string.allow_to_install_intervirt),
        )
        GeneralSpacer(8.dp)
        Button(
            onClick = {
                if (job != null) {
                    job?.cancel()
                    job = null
                    flow = null
                } else {
                    flow = flow {
                        // Creating Intervirt folder
                        emit(
                            ResultProgress.proceed(
                                percentage = 0.05f,
                                message = getString(Res.string.creating_intervirt_folder),
                            ),
                        )
                        fileManager.init()
                        // Applying configuration
                        emit(
                            ResultProgress.proceed(
                                percentage = 0.1f,
                                message = getString(Res.string.applying_configuration),
                            ),
                        )
                        // Apply configuration
                        // Downloading QEMU
                        downloader.downloadQemu().collect {
                            emit(it.clone(percentage = it.percentage * 0.4f + 0.1f))
                            if (it is ResultProgress.Result && it.result.isFailure) job!!.cancel()
                        }
                        // Downloading disk
                        downloader.downloadAlpineDisk().collect {
                            emit(it.clone(percentage = it.percentage * 0.5f + 0.5f))
                            if (it is ResultProgress.Result && it.result.isFailure) job!!.cancel()
                        }
                        appEnv.INSTALLED = true
                        appState.currentScreenIndex = 1
                    }
                }
            },
            enabled = allowInstallation,
            colors = if (job != null) ButtonDefaults.buttonColors(containerColor = Color.Red) else ButtonDefaults.buttonColors(),
        ) {
            if (job != null) Text(
                text = stringResource(Res.string.cancel_intervirt_installation),
                color = Color.White,
            ) else Text(
                text = stringResource(Res.string.install_intervirt),
            )
        }
        AnimatedVisibility(
            visible = flow != null,
        ) {
            GeneralSpacer(8.dp)
            FlowProgressView(
                flow = flow ?: flowOf(),
                onJobChange = { job = it },
            ) { resultProgress ->
                logger.debug { "SETUP: $resultProgress" }
                appState.logs.add(resultProgress.log())
            }
        }
        GeneralSpacer(8.dp)
        NamedCheckbox(
            checked = appState.showLogs,
            onCheckedChange = { appState.showLogs = it },
            name = stringResource(Res.string.show_logs),
        )
    }
}
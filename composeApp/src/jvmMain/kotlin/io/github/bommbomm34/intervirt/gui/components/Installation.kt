package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.data.FileManager
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.logger
import io.github.bommbomm34.intervirt.logs
import io.github.bommbomm34.intervirt.setup.Downloader
import io.github.bommbomm34.intervirt.showLogs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

// TODO: Finish setup script
@Composable
fun Installation(applyConfiguration: () -> Unit) {
    var allowInstallation by remember { mutableStateOf(false) }
    var flow: Flow<ResultProgress<String>>? by remember { mutableStateOf(null) }
    CenterColumn {
        NamedCheckbox(
            checked = allowInstallation,
            onCheckedChange = { allowInstallation = it },
            name = stringResource(Res.string.allow_to_install_intervirt),
        )
        GeneralSpacer()
        Button(
            onClick = {
                flow = flow {
                    // Creating Intervirt folder
                    emit(
                        ResultProgress.proceed(
                            percentage = 0.05f,
                            message = getString(Res.string.creating_intervirt_folder)
                        )
                    )
                    FileManager.init()
                    // Applying configuration
                    emit(
                        ResultProgress.proceed(
                            percentage = 0.1f,
                            message = getString(Res.string.applying_configuration)
                        )
                    )
                    applyConfiguration()
                    // Downloading QEMU
                    Downloader.downloadQEMU().collect { emit(it.copy(percentage = it.percentage * 0.9f + 0.1f)) }
                }
            },
            enabled = allowInstallation
        ) { Text(stringResource(Res.string.install_intervirt)) }
        flow?.let { flow ->
            GeneralSpacer(8.dp)
            FlowProgressView(
                flow = flow
            ) { resultProgress ->
                logger.debug { "SETUP: $resultProgress" }
                resultProgress.message?.let { logs.add(it) }
            }
        }
        GeneralSpacer(8.dp)
        NamedCheckbox(
            checked = showLogs,
            onCheckedChange = { showLogs = it },
            name = stringResource(Res.string.show_logs),
        )
    }
}
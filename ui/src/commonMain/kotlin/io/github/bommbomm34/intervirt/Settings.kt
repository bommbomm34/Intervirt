package io.github.bommbomm34.intervirt

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.restart_necessary_to_apply_changes
import intervirt.ui.generated.resources.save_changes
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.BackButton
import io.github.bommbomm34.intervirt.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.components.configuration.DebugOptions
import io.github.bommbomm34.intervirt.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog
import io.github.bommbomm34.intervirt.core.api.Preferences
import io.github.bommbomm34.intervirt.core.data.AppConfigurationData
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.VMConfigurationData
import io.github.bommbomm34.intervirt.data.AppState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.util.*
import kotlin.system.exitProcess

@Composable
fun Settings() {
    val appEnv = koinInject<AppEnv>()
    val appState = koinInject<AppState>()
    val windowSize = appState.windowState.size
    val isDarkMode = appEnv.isDarkMode()
    var appConf by remember {
        mutableStateOf(
            AppConfigurationData(
                vmShutdownTimeout = appEnv.VM_SHUTDOWN_TIMEOUT.toInt(),
                agentPort = appEnv.AGENT_PORT,
                intervirtFolder = appEnv.DATA_DIR.absolutePath,
                darkMode = appEnv.DARK_MODE ?: isDarkMode,
                language = appEnv.LANGUAGE.toLanguageTag(),
                accentColor = appEnv.ACCENT_COLOR,
            ),
        )
    }
    var vmConf by remember {
        mutableStateOf(
            VMConfigurationData(
                ram = appEnv.VM_RAM,
                cpu = appEnv.VM_CPU,
                kvm = appEnv.VM_ENABLE_KVM,
                diskUrl = appEnv.VM_DISK_URL,
                diskHashUrl = appEnv.VM_DISK_HASH_URL,
            ),
        )
    }
    val confHash = remember { Objects.hash(appConf, vmConf) }
    AlignedBox(Alignment.TopStart) {
        BackButton {
            appState.currentScreenIndex = 1
        }
    }
    AlignedBox(Alignment.Center) {
        CenterColumn(
            modifier = Modifier
                .size(windowSize * 0.8f)
                .verticalScroll(rememberScrollState())
        ) {
            AppConfiguration(appConf) {
                appConf = it
            }
            GeneralSpacer()
            VMConfiguration(vmConf) {
                vmConf = it
            }
            GeneralSpacer()
            Button(
                onClick = {
                    println("Apply conf")
                    appEnv.applyConfiguration(vmConf, appConf)
                    appState.openDialog {
                        AcceptDialog(
                            message = stringResource(Res.string.restart_necessary_to_apply_changes),
                        ) {
                            exitProcess(0)
                        }
                    }
                },
                enabled = confHash != Objects.hash(appConf, vmConf),
            ) {
                Text(stringResource(Res.string.save_changes))
            }
            GeneralSpacer()
            if (appEnv.DEBUG_ENABLED) DebugOptions()
        }
    }
}
package io.github.bommbomm34.intervirt.gui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.restart_necessary_to_apply_changes
import intervirt.composeapp.generated.resources.save_changes
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.applyConfiguration
import io.github.bommbomm34.intervirt.data.AppConfigurationData
import io.github.bommbomm34.intervirt.data.AppEnv
import io.github.bommbomm34.intervirt.data.VMConfigurationData
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.gui.components.AcceptDialog
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.BackButton
import io.github.bommbomm34.intervirt.gui.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.gui.components.configuration.DebugOptions
import io.github.bommbomm34.intervirt.gui.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.isDarkMode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.util.*
import kotlin.system.exitProcess

@Composable
fun Settings() {
    val appEnv = koinInject<AppEnv>()
    val preferences = koinInject<Preferences>()
    val appState = koinInject<AppState>()
    val isDarkMode = appEnv.isDarkMode()
    var appConf by remember {
        mutableStateOf(
            AppConfigurationData(
                vmShutdownTimeout = appEnv.VM_SHUTDOWN_TIMEOUT.toInt(),
                agentPort = appEnv.AGENT_PORT,
                intervirtFolder = appEnv.DATA_DIR.absolutePath,
                darkMode = appEnv.DARK_MODE ?: isDarkMode,
                language = appEnv.LANGUAGE.toLanguageTag()
            )
        )
    }
    var vmConf by remember {
        mutableStateOf(
            VMConfigurationData(
                ram = appEnv.VM_RAM,
                cpu = appEnv.VM_CPU,
                kvm = appEnv.VM_ENABLE_KVM
            )
        )
    }
    val confHash = remember { Objects.hash(appConf, vmConf) }
    AlignedBox(Alignment.TopStart){
        BackButton { appState.currentScreenIndex = 1 }
    }
    AlignedBox(Alignment.Center){
        CenterColumn {
            AppConfiguration(appConf){ appConf = it }
            GeneralSpacer()
            VMConfiguration(vmConf){ vmConf = it }
            GeneralSpacer()
            Button(
                onClick = {
                    preferences.applyConfiguration(vmConf, appConf)
                    appState.openDialog {
                        AcceptDialog(
                            message = stringResource(Res.string.restart_necessary_to_apply_changes),
                        ){
                            exitProcess(0)
                        }
                    }
                },
                enabled = confHash != Objects.hash(appConf, vmConf)
            ){
                Text(stringResource(Res.string.save_changes))
            }
            GeneralSpacer()
            if (appEnv.DEBUG_ENABLED) DebugOptions()
        }
    }
}
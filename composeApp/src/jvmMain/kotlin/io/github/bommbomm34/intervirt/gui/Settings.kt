package io.github.bommbomm34.intervirt.gui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.restart_necessary_to_apply_changes
import intervirt.composeapp.generated.resources.save_changes
import io.github.bommbomm34.intervirt.applyConfiguration
import io.github.bommbomm34.intervirt.core.api.Preferences
import io.github.bommbomm34.intervirt.core.data.AppConfigurationData
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.VMConfigurationData
import io.github.bommbomm34.intervirt.data.AppState
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
    val preferences = koinInject <Preferences>()
    val appState = koinInject<AppState>()
    val isDarkMode = appEnv.isDarkMode()
    var appConf by remember {
        mutableStateOf(
            AppConfigurationData(
                vmShutdownTimeout = appEnv.vmShutdownTimeout.toInt(),
                agentPort = appEnv.agentPort,
                intervirtFolder = appEnv.dataDir.absolutePath,
                darkMode = appEnv.darkMode ?: isDarkMode,
                language = appEnv.language.toLanguageTag()
            )
        )
    }
    var vmConf by remember {
        mutableStateOf(
            VMConfigurationData(
                ram = appEnv.vmRam,
                cpu = appEnv.vmCpu,
                kvm = appEnv.vmEnableKvm,
                diskUrl = appEnv.vmDiskUrl,
                diskHashUrl = appEnv.vmDiskHashUrl
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
            if (appEnv.debugEnabled) DebugOptions()
        }
    }
}
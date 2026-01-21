package io.github.bommbomm34.intervirt.gui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.restart_necessary_to_apply_changes
import intervirt.composeapp.generated.resources.save_changes
import io.github.bommbomm34.intervirt.applyConfiguration
import io.github.bommbomm34.intervirt.currentScreenIndex
import io.github.bommbomm34.intervirt.data.AppConfigurationData
import io.github.bommbomm34.intervirt.data.Preferences
import io.github.bommbomm34.intervirt.data.VMConfigurationData
import io.github.bommbomm34.intervirt.gui.components.AcceptDialog
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.BackButton
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.gui.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.gui.components.configuration.DebugOptions
import io.github.bommbomm34.intervirt.gui.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.isDarkMode
import io.github.bommbomm34.intervirt.openDialog
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.util.Objects
import kotlin.system.exitProcess

@Composable
fun Settings() {
    val preferences = koinInject<Preferences>()
    val isDarkMode = preferences.isDarkMode()
    var appConf by remember {
        mutableStateOf(
            AppConfigurationData(
                vmShutdownTimeout = preferences.VM_SHUTDOWN_TIMEOUT.toInt(),
                agentPort = preferences.AGENT_PORT,
                intervirtFolder = preferences.DATA_DIR.absolutePath,
                darkMode = preferences.DARK_MODE ?: isDarkMode,
                language = preferences.LANGUAGE.toLanguageTag()
            )
        )
    }
    var vmConf by remember {
        mutableStateOf(
            VMConfigurationData(
                ram = preferences.VM_RAM,
                cpu = preferences.VM_CPU,
                kvm = preferences.VM_ENABLE_KVM
            )
        )
    }
    val confHash = remember { Objects.hash(appConf, vmConf) }
    AlignedBox(Alignment.TopStart){
        BackButton { currentScreenIndex = 1 }
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
                    openDialog {
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
            if (preferences.DEBUG_ENABLED) DebugOptions()
        }
    }
}
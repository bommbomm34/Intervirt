package io.github.bommbomm34.intervirt

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.restart_necessary_to_apply_changes
import intervirt.ui.generated.resources.save_changes
import io.github.bommbomm34.intervirt.applyConfiguration
import io.github.bommbomm34.intervirt.core.api.Preferences
import io.github.bommbomm34.intervirt.core.data.AppConfigurationData
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.VMConfigurationData
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.dialogs.AcceptDialog
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
                language = appEnv.LANGUAGE.toLanguageTag(),
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
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.TopStart) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.BackButton {
            appState.currentScreenIndex = 1
        }
    }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.Center) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterColumn {
            _root_ide_package_.io.github.bommbomm34.intervirt.components.configuration.AppConfiguration(appConf) {
                appConf = it
            }
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            _root_ide_package_.io.github.bommbomm34.intervirt.components.configuration.VMConfiguration(vmConf) {
                vmConf = it
            }
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            Button(
                onClick = {
                    appEnv.applyConfiguration(vmConf, appConf)
                    appState.openDialog {
                        _root_ide_package_.io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog(
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
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            if (appEnv.DEBUG_ENABLED) _root_ide_package_.io.github.bommbomm34.intervirt.components.configuration.DebugOptions()
        }
    }
}
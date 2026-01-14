package io.github.bommbomm34.intervirt.gui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.restart_necessary_to_apply_changes
import intervirt.composeapp.generated.resources.save_changes
import io.github.bommbomm34.intervirt.AGENT_PORT
import io.github.bommbomm34.intervirt.DATA_DIR
import io.github.bommbomm34.intervirt.VM_CPU
import io.github.bommbomm34.intervirt.VM_ENABLE_KVM
import io.github.bommbomm34.intervirt.VM_RAM
import io.github.bommbomm34.intervirt.VM_SHUTDOWN_TIMEOUT
import io.github.bommbomm34.intervirt.applyConfiguration
import io.github.bommbomm34.intervirt.data.AppConfigurationData
import io.github.bommbomm34.intervirt.data.VMConfigurationData
import io.github.bommbomm34.intervirt.gui.components.AcceptDialog
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.gui.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.openDialog
import org.jetbrains.compose.resources.stringResource
import java.util.Objects
import kotlin.system.exitProcess

@Composable
fun Settings() {
    var appConf by remember {
        mutableStateOf(
            AppConfigurationData(
                vmShutdownTimeout = VM_SHUTDOWN_TIMEOUT.toInt(),
                agentPort = AGENT_PORT,
                intervirtFolder = DATA_DIR.absolutePath
            )
        )
    }
    var vmConf by remember {
        mutableStateOf(
            VMConfigurationData(
                ram = VM_RAM,
                cpu = VM_CPU,
                kvm = VM_ENABLE_KVM
            )
        )
    }
    val confHash = remember { Objects.hash(appConf, vmConf) }
    AlignedBox(Alignment.Center){
        CenterColumn {
            AppConfiguration(appConf){ appConf = it }
            GeneralSpacer()
            VMConfiguration(vmConf){ vmConf = it }
            GeneralSpacer()
            Button(
                onClick = {
                    applyConfiguration(vmConf, appConf)
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
        }
    }
}
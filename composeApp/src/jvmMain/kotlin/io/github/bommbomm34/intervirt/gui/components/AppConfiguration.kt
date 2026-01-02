package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.agent_port
import intervirt.composeapp.generated.resources.intervirt_folder
import intervirt.composeapp.generated.resources.ram_in_mb
import intervirt.composeapp.generated.resources.vm_shutdown_timeout
import io.github.bommbomm34.intervirt.data.AppConfigurationData
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppConfiguration(
    conf: AppConfigurationData,
    onConfChange: (AppConfigurationData) -> Unit
){
    CenterColumn {
        IntegerTextField(
            value = conf.vmShutdownTimeout,
            onValueChange = { onConfChange(conf.copy(vmShutdownTimeout = it)) },
            label = stringResource(Res.string.vm_shutdown_timeout)
        )
        GeneralSpacer()
        IntegerTextField(
            value = conf.agentPort,
            onValueChange = { onConfChange(conf.copy(agentPort = it)) },
            label = stringResource(Res.string.agent_port)
        )
        GeneralSpacer()
        OutlinedTextField(
            value = conf.intervirtFolder,
            onValueChange = { onConfChange(conf.copy(intervirtFolder = it)) },
            label = { Text(stringResource(Res.string.intervirt_folder)) }
        )
    }
}
package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.Composable
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.agent_port
import intervirt.composeapp.generated.resources.dark_mode
import intervirt.composeapp.generated.resources.intervirt_folder
import intervirt.composeapp.generated.resources.vm_shutdown_timeout
import io.github.bommbomm34.intervirt.DATA_DIR
import io.github.bommbomm34.intervirt.data.AppConfigurationData
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.FilePicker
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.IntegerTextField
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.vinceglb.filekit.absolutePath
import org.jetbrains.compose.resources.stringResource
import java.util.Locale

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
        FilePicker(
            label = stringResource(Res.string.intervirt_folder),
            directory = true,
            defaultPath = DATA_DIR.absolutePath
        ) { onConfChange(conf.copy(intervirtFolder = it.absolutePath())) }
        GeneralSpacer()
        NamedCheckbox(
            checked = conf.darkMode,
            onCheckedChange = { onConfChange(conf.copy(darkMode = it)) },
            name = stringResource(Res.string.dark_mode)
        )
        GeneralSpacer()
        LanguagePicker(
            language = Locale.forLanguageTag(conf.language),
            onChangeLanguage = { onConfChange(conf.copy(language = it.toLanguageTag())) }
        )
    }
}
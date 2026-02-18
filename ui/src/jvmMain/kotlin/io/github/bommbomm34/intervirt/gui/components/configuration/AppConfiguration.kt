package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.data.AppConfigurationData
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.FilePicker
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.components.textfields.IntegerTextField
import io.github.vinceglb.filekit.absolutePath
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.util.*

@Composable
fun AppConfiguration(
    conf: AppConfigurationData,
    onConfChange: (AppConfigurationData) -> Unit,
) {
    val appEnv = koinInject<AppEnv>()
    CenterColumn {
        IntegerTextField(
            value = conf.vmShutdownTimeout,
            onValueChange = { onConfChange(conf.copy(vmShutdownTimeout = it)) },
            label = stringResource(Res.string.vm_shutdown_timeout),
        )
        GeneralSpacer()
        IntegerTextField(
            value = conf.agentPort,
            onValueChange = { onConfChange(conf.copy(agentPort = it)) },
            label = stringResource(Res.string.agent_port),
        )
        GeneralSpacer()
        FilePicker(
            label = stringResource(Res.string.intervirt_folder),
            directory = true,
            defaultPath = appEnv.dataDir.absolutePath,
        ) { onConfChange(conf.copy(intervirtFolder = it.absolutePath())) }
        GeneralSpacer()
        NamedCheckbox(
            checked = conf.darkMode,
            onCheckedChange = { onConfChange(conf.copy(darkMode = it)) },
            name = stringResource(Res.string.dark_mode),
        )
        GeneralSpacer()
        LanguagePicker(
            language = Locale.forLanguageTag(conf.language),
            onChangeLanguage = { onConfChange(conf.copy(language = it.toLanguageTag())) },
        )
    }
}
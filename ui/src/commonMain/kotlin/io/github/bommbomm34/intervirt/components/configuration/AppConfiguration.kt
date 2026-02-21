package io.github.bommbomm34.intervirt.components.configuration

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.*
import io.github.bommbomm34.intervirt.components.textfields.IntegerTextField
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.isDarkMode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AppConfiguration(appEnv: AppEnv) {
    CenterColumn {
        IntegerTextField(
            value = appEnv.VM_SHUTDOWN_TIMEOUT.toInt(),
            onValueChange = { appEnv.VM_SHUTDOWN_TIMEOUT = it.toLong() },
            label = stringResource(Res.string.vm_shutdown_timeout),
        )
        GeneralSpacer()
        IntegerTextField(
            value = appEnv.AGENT_PORT,
            onValueChange = { appEnv.AGENT_PORT = it },
            label = stringResource(Res.string.agent_port),
        )
        GeneralSpacer()
        FilePicker(
            label = stringResource(Res.string.intervirt_folder),
            directory = true,
            defaultPath = appEnv.DATA_DIR.absolutePath,
        ) { appEnv.DATA_DIR = it.file }
        GeneralSpacer()
        NamedCheckbox(
            checked = appEnv.isDarkMode(),
            onCheckedChange = { appEnv.DARK_MODE = it },
            name = stringResource(Res.string.dark_mode),
        )
        GeneralSpacer()
        LanguagePicker(
            language = appEnv.LANGUAGE,
            onChangeLanguage = { appEnv.LANGUAGE = it },
        )
        GeneralSpacer()
        ColorPicker(
            color = Color(appEnv.ACCENT_COLOR),
            onColorSelect = { appEnv.ACCENT_COLOR = it.value },
        )
    }
}
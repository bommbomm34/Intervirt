package io.github.bommbomm34.intervirt.components.configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.*
import io.github.bommbomm34.intervirt.components.textfields.IntegerTextField
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.isDarkMode
import io.github.bommbomm34.intervirt.state
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AppConfiguration(appEnv: AppEnv) {
    val vmShutdownTimeout by appEnv.state { ::VM_SHUTDOWN_TIMEOUT }
    val agentPort by appEnv.state { ::AGENT_PORT }
    val dataDir by appEnv.state { ::DATA_DIR }
    val language by appEnv.state { ::LANGUAGE }
    val accentColor by appEnv.state { ::ACCENT_COLOR }
    CenterColumn {
        IntegerTextField(
            value = vmShutdownTimeout.toInt(),
            onValueChange = { appEnv.VM_SHUTDOWN_TIMEOUT = it.toLong() },
            label = stringResource(Res.string.vm_shutdown_timeout),
        )
        GeneralSpacer()
        IntegerTextField(
            value = agentPort,
            onValueChange = { appEnv.AGENT_PORT = it },
            label = stringResource(Res.string.agent_port),
        )
        GeneralSpacer()
        FilePicker(
            label = stringResource(Res.string.intervirt_folder),
            directory = true,
            defaultPath = dataDir.absolutePath,
        ) { appEnv.DATA_DIR = it.file }
        GeneralSpacer()
        NamedCheckbox(
            checked = appEnv.isDarkMode(),
            onCheckedChange = { appEnv.DARK_MODE = it },
            name = stringResource(Res.string.dark_mode),
        )
        GeneralSpacer()
        LanguagePicker(
            language = language,
            onChangeLanguage = { appEnv.LANGUAGE = it },
        )
        GeneralSpacer()
        ColorPicker(
            color = Color(accentColor),
            onColorSelect = { appEnv.ACCENT_COLOR = it.value },
        )
    }
}
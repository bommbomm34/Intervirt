/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.*
import io.github.bommbomm34.intervirt.components.textfields.IntegerTextField
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.isDarkMode
import io.github.vinceglb.filekit.absolutePath
import org.jetbrains.compose.resources.stringResource
import java.util.Locale

@Composable
fun AppConfiguration(
    appEnv: AppEnv,
    onEnvChange: (AppEnv) -> Unit,
) {
    CenterColumn {
        IntegerTextField(
            value = appEnv.vmShutdownTimeout.toInt(),
            onValueChange = {
                onEnvChange(appEnv.copy(vmShutdownTimeout = it.toLong()))
            },
            label = stringResource(Res.string.vm_shutdown_timeout),
        )
        GeneralSpacer()
        IntegerTextField(
            value = appEnv.agentPort,
            onValueChange = {
                onEnvChange(appEnv.copy(agentPort = it))
            },
            label = stringResource(Res.string.agent_port),
        )
        GeneralSpacer()
        FilePicker(
            label = stringResource(Res.string.intervirt_folder),
            directory = true,
            defaultPath = appEnv.dataDir,
        ) {
            onEnvChange(appEnv.copy(dataDir = it.absolutePath()))
        }
        GeneralSpacer()
        NamedCheckbox(
            checked = appEnv.isDarkMode(),
            onCheckedChange = {
                onEnvChange(appEnv.copy(darkMode = it))
            },
            name = stringResource(Res.string.dark_mode),
        )
        GeneralSpacer()
        LanguagePicker(
            language = Locale.forLanguageTag(appEnv.language),
            onChangeLanguage = {
                onEnvChange(appEnv.copy(language = it.toLanguageTag()))
            },
        )
        GeneralSpacer()
        ColorPicker(
            color = Color(appEnv.accentColor),
            onColorSelect = {
                onEnvChange(appEnv.copy(accentColor = it.value))
            },
        )
    }
}

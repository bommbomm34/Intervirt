/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.save_changes
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.BackButton
import io.github.bommbomm34.intervirt.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.components.configuration.DebugOptions
import io.github.bommbomm34.intervirt.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Screen
import io.github.bommbomm34.intervirt.model.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Settings() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val appState = koinInject<AppState>()
    val windowSize = appState.windowState.size
    AlignedBox(Alignment.TopStart) {
        BackButton {
            appState.currentScreen = Screen.HOME
        }
    }
    AlignedBox(Alignment.Center) {
        CenterColumn(
            modifier = Modifier
                .size(windowSize * 0.8f)
                .verticalScroll(rememberScrollState()),
        ) {
            AppConfiguration(viewModel.appEnv) { viewModel.appEnv = it }
            GeneralSpacer()
            VMConfiguration(viewModel.appEnv) { viewModel.appEnv = it }
            GeneralSpacer()
            Button(
                onClick = viewModel::saveChanges,
                enabled = viewModel.changed,
            ) {
                Text(stringResource(Res.string.save_changes))
            }
            GeneralSpacer()
            if (currentAppEnv.debugEnabled) DebugOptions()
        }
    }
}

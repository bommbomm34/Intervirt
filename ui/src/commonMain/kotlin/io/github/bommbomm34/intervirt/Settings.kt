package io.github.bommbomm34.intervirt

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.AppState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Settings() {
    val appState = koinInject<AppState>()
    val windowSize = appState.windowState.size
    var changed by remember { mutableStateOf(false) }
    val baseAppEnv = koinInject<AppEnv>()
    val appEnv = remember {
        baseAppEnv.copy(
            autoFlush = false,
            onChange = { changed = true },
        )
    }
    AlignedBox(Alignment.TopStart) {
        BackButton {
            appState.currentScreenIndex = 1
        }
    }
    AlignedBox(Alignment.Center) {
        CenterColumn(
            modifier = Modifier
                .size(windowSize * 0.8f)
                .verticalScroll(rememberScrollState()),
        ) {
            AppConfiguration(appEnv)
            GeneralSpacer()
            VMConfiguration(appEnv)
            GeneralSpacer()
            Button(
                onClick = {
                    appEnv.flush()
                    baseAppEnv.invalidateCache()
                    appState.appEnvChangeKey++
                },
                enabled = changed,
            ) {
                Text(stringResource(Res.string.save_changes))
            }
            GeneralSpacer()
            if (appEnv.DEBUG_ENABLED) DebugOptions()
        }
    }
}
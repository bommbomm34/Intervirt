package io.github.bommbomm34.intervirt

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.MultipleAnimatedVisibility
import org.koin.compose.koinInject

@Composable
fun App() {
    val appState = koinInject<AppState>()
    _root_ide_package_.io.github.bommbomm34.intervirt.components.MultipleAnimatedVisibility(
        visible = appState.currentScreenIndex,
        screens = listOf(
            { Setup() },
            { Home() },
            { Settings() },
            { About() },
        ),
    )
}
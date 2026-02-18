package io.github.bommbomm34.intervirt.gui

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.MultipleAnimatedVisibility
import org.koin.compose.koinInject

@Composable
fun App() {
    val appState = koinInject<AppState>()
    MultipleAnimatedVisibility(
        visible = appState.currentScreenIndex,
        screens = listOf(
            { Setup() },
            { Home() },
            { Settings() },
            { About() },
        ),
    )
}
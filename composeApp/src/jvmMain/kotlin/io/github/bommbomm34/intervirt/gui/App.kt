package io.github.bommbomm34.intervirt.gui

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.currentScreenIndex
import io.github.bommbomm34.intervirt.data.Preferences
import io.github.bommbomm34.intervirt.env
import io.github.bommbomm34.intervirt.gui.components.MultipleAnimatedVisibility

@Composable
fun App() {
    MultipleAnimatedVisibility(
        visible = currentScreenIndex,
        screens = listOf(
            { Setup() },
            { Home() },
            { OSInstaller() },
            { Settings() },
            { About() }
        )
    )
}
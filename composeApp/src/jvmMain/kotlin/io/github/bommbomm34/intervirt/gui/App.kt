package io.github.bommbomm34.intervirt.gui

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.data.Preferences
import io.github.bommbomm34.intervirt.env
import io.github.bommbomm34.intervirt.gui.components.MultipleAnimatedVisibility

@Composable
fun App() {
    var currentScreenIndex by remember { mutableStateOf(if (checkSetupStatus()) 1 else 0) }
    MultipleAnimatedVisibility(
        visible = currentScreenIndex,
        screens = listOf(
            { Setup { currentScreenIndex = 1 } },
            { Home() },
            { OSInstaller() },
            { Settings() }
        )
    )
}

fun checkSetupStatus() = env("INSTALLED").toBoolean()
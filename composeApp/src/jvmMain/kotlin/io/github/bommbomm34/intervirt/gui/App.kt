package io.github.bommbomm34.intervirt.gui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.data.FileManager
import io.github.bommbomm34.intervirt.env
import io.github.bommbomm34.intervirt.gui.components.MultipleAnimatedVisibility

@Composable
fun App() {
    var currentScreenIndex by remember { mutableStateOf(if (checkSetupStatus()) 1 else 0) }
    MultipleAnimatedVisibility(
        visible = currentScreenIndex,
        screens = listOf(
            { Setup() },
            { Home() },
            { OSInstaller() },
            { Settings() }
        )
    )
}

fun checkSetupStatus() = FileManager.getFile("disk/alpine_linux.qcow2").exists()
package io.github.bommbomm34.intervirt.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.data.FileManager
import io.github.bommbomm34.intervirt.env

@Composable
fun App() {
    var currentScreenIndex by remember { mutableStateOf(if (checkSetupStatus()) 1 else 0) }
    val colors = if (env("DARK_THEME")?.toBoolean() ?: isSystemInDarkTheme()) darkColors() else lightColors()
    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography.copy(
            h1 = MaterialTheme.typography.h1.copy(colors.onBackground),
            h2 = MaterialTheme.typography.h2.copy(colors.onBackground),
            h3 = MaterialTheme.typography.h3.copy(colors.onBackground),
            h4 = MaterialTheme.typography.h4.copy(colors.onBackground),
            h5 = MaterialTheme.typography.h5.copy(colors.onBackground),
            h6 = MaterialTheme.typography.h6.copy(colors.onBackground),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(colors.onBackground),
            subtitle2 = MaterialTheme.typography.subtitle2.copy(colors.onBackground),
            body1 = MaterialTheme.typography.body1.copy(colors.onBackground),
            body2 = MaterialTheme.typography.body2.copy(colors.onBackground),
            button = MaterialTheme.typography.button.copy(colors.onPrimary),
            caption = MaterialTheme.typography.caption.copy(colors.onBackground),
            overline = MaterialTheme.typography.overline.copy(colors.onBackground)
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .safeContentPadding()
                .background(colors.background)
        ){
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                MultipleAnimatedVisibility(
                    currentScreenIndex,
                    listOf(
                        { Setup() },
                        { Home() },
                        { OSInstaller() },
                        { RegularSettings() },
                        { AdvancedSettings() }
                    )
                )
            }
        }
    }
}

fun checkSetupStatus() = FileManager.getFile("disk/alpine_linux.qcow2").exists()
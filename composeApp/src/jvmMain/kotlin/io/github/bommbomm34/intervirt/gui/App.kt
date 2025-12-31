package io.github.bommbomm34.intervirt.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.data.FileManager
import io.github.bommbomm34.intervirt.env

@Composable
fun App(){
    var currentScreenIndex by remember { mutableStateOf(if (checkSetupStatus()) 1 else 0) }
    val colors = if (env("DARK_THEME")?.toBoolean() ?: isSystemInDarkTheme()) darkColors() else lightColors()
    MaterialTheme (
        colors = colors,
        typography = MaterialTheme.typography.copy(
            h1 = MaterialTheme.typography.h1.copy(colors.primary),
            h2 = MaterialTheme.typography.h2.copy(colors.primary),
            h3 = MaterialTheme.typography.h3.copy(colors.primary),
            h4 = MaterialTheme.typography.h4.copy(colors.primary),
            h5 = MaterialTheme.typography.h5.copy(colors.primary),
            h6 = MaterialTheme.typography.h6.copy(colors.primary),
            subtitle1 = MaterialTheme.typography.h1.copy(colors.primary),
            subtitle2 = MaterialTheme.typography.h2.copy(colors.primary),
            body1 = MaterialTheme.typography.h3.copy(colors.primary),
            body2 = MaterialTheme.typography.h4.copy(colors.primary),
            button = MaterialTheme.typography.h5.copy(colors.primary),
            caption = MaterialTheme.typography.h6.copy(colors.primary),
            overline = MaterialTheme.typography.h6.copy(colors.primary)
        )
    ) {
        Column (
            Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(16.dp)
                .background(colors.secondary)
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

fun checkSetupStatus() = FileManager.getFile("disk/alpine_linux.qcow2").exists()
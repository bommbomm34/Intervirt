/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.components.MultipleAnimatedVisibility
import io.github.bommbomm34.intervirt.data.AppState
import org.koin.compose.koinInject

@Composable
fun App() {
    val appState = koinInject<AppState>()
    MultipleAnimatedVisibility(
        visible = appState.currentScreen.ordinal,
        screens = listOf(
            { Setup() },
            { Home() },
            { Settings() },
            { About() },
        ),
    )
}

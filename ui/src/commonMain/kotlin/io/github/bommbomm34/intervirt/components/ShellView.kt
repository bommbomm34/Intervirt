/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components

import ai.rever.bossterm.compose.EmbeddableTerminal
import ai.rever.bossterm.compose.rememberEmbeddableTerminalState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.impl.ContainerSshClient
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.impl.ContainerPlatformServices
import org.koin.compose.koinInject

@Composable
fun ShellView(ioClient: ContainerIOClient) {
    val appState = koinInject<AppState>()
    if (ioClient is ContainerSshClient) {
        val state = rememberEmbeddableTerminalState()
        val platformServices = remember(ioClient) { ContainerPlatformServices(ioClient) }
        EmbeddableTerminal(
            state = state,
            platformServices = platformServices,
        )
        DisposableEffect(Unit) {
            onDispose {
                state.dispose()
            }
        }
    } else appState.openDialog(
        severity = Severity.WARNING,
        message = "Currently, PTY Shell isn't supported on virtual containers",
    )
}

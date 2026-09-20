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
import androidx.compose.runtime.rememberCoroutineScope
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.impl.ContainerSshClient
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.data.runDialogCatching
import io.github.bommbomm34.intervirt.data.showFailureDialog
import io.github.bommbomm34.intervirt.impl.ContainerPlatformServices
import io.github.bommbomm34.intervirt.logging.debug
import io.github.bommbomm34.intervirt.logging.error
import io.github.bommbomm34.intervirt.logging.warn
import io.github.bommbomm34.intervirt.rememberLogger
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ShellView(
    ioClient: ContainerIOClient,
    onClose: () -> Unit,
) {
    val appEnv = currentAppEnv
    val appState = koinInject<AppState>()
    val logger = rememberLogger("ShellView")

    if (ioClient is ContainerSshClient) {
        val scope = rememberCoroutineScope()
        val state = rememberEmbeddableTerminalState()
        val platformServices = remember(ioClient) {
            ContainerPlatformServices(appEnv, ioClient) { error ->
                scope.launch {
                    appState.showFailureDialog(error)
                }
            }
        }
        EmbeddableTerminal(
            state = state,
            platformServices = platformServices,
            onExit = { statusCode ->
                if (statusCode != 0) {
                    logger.warn { "Unexpected terminal exit code: $statusCode" }
                }
                scope.launchDialogCatching(appState) {
                    platformServices.close()
                }
                onClose()
            },
        )
    } else appState.openDialog(
        severity = Severity.WARNING,
        message = "Currently, PTY Shell isn't supported on virtual containers",
    )
}

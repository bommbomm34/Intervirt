package io.github.bommbomm34.intervirt.components

import ai.rever.bossterm.compose.EmbeddableTerminal
import ai.rever.bossterm.compose.rememberEmbeddableTerminalState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.impl.ContainerPlatformServices

@Composable
fun ShellView(ioClient: ContainerIOClient) {
    val state = rememberEmbeddableTerminalState()
    val platformServices = remember { ContainerPlatformServices(ioClient) }
    val port = remember { ioClient.port }
    EmbeddableTerminal(
        state = state,
        initialCommand = "ssh root@ -p $port",
        platformServices = platformServices,
    )
    DisposableEffect(Unit) {
        onDispose {
            state.dispose()
        }
    }
}
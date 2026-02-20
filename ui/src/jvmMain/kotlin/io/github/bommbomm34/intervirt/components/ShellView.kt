package io.github.bommbomm34.intervirt.components

import ai.rever.bossterm.compose.EmbeddableTerminal
import ai.rever.bossterm.compose.rememberEmbeddableTerminalState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient

@Composable
fun ShellView(ioClient: ContainerIOClient) {
    val state = rememberEmbeddableTerminalState()
    val port = remember { ioClient.port }
    // TODO: If PR #245 of BossTerm gets merged, replace this with a PlatformServices implementation
    EmbeddableTerminal(
        state = state,
        initialCommand = "ssh root@ -p $port",
    )
    DisposableEffect(Unit){
        onDispose {
            state.dispose()
        }
    }
}
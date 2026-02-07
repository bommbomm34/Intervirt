package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.enable_virtual_hosts
import io.github.bommbomm34.intervirt.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.components.buttons.PlayButton
import io.github.bommbomm34.intervirt.gui.intervirtos.http.VirtualHostsManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun HttpServer(
    osClient: IntervirtOSClient
) {
    val scope = rememberCoroutineScope()
    val appState = koinInject<AppState>()
    var running by remember { mutableStateOf(false) }
    var enableVirtualHosts by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appState.runDialogCatching {
            running = osClient.isHttpServerActive().getOrThrow()
        }
    }
    AlignedBox(Alignment.TopEnd) {
        PlayButton(running) { enabled ->
            scope.launch {
                appState.runDialogCatching {
                    osClient.enableHttpServer(enabled)
                }.onSuccess { running = enabled }
            }
        }
    }
    NamedCheckbox(
        checked = enableVirtualHosts,
        onCheckedChange = { enableVirtualHosts = it },
        name = stringResource(Res.string.enable_virtual_hosts)
    )
    AnimatedVisibility(enableVirtualHosts) {
        VirtualHostsManager(osClient)
    }
}
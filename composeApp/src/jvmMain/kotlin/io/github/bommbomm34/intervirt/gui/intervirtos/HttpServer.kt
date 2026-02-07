package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.enable_virtual_hosts
import io.github.bommbomm34.intervirt.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.components.buttons.PlayButton
import io.github.bommbomm34.intervirt.gui.intervirtos.http.VirtualHostsManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun HttpServer(
    osClient: IntervirtOSClient
) {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var enableVirtualHosts by remember { mutableStateOf(false) }
    AlignedBox(Alignment.TopEnd) {
        PlayButton(running) {
            scope.launch { osClient.enableHttpServer(it) }
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
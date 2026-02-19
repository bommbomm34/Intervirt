package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.enable_virtual_hosts
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.HttpServerManager
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.intervirtos.components.DockerContainerView
import io.github.bommbomm34.intervirt.gui.intervirtos.components.SystemServiceView
import io.github.bommbomm34.intervirt.gui.intervirtos.http.VirtualHostsManager
import io.github.bommbomm34.intervirt.initialize
import io.github.bommbomm34.intervirt.rememberManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun HttpServer(
    osClient: IntervirtOSClient,
) {
    val httpServer = osClient.rememberManager(::HttpServerManager)
    val initialized by httpServer.initialize()
    var enableVirtualHosts by remember { mutableStateOf(false) }
    if (initialized){
        AlignedBox(Alignment.TopEnd) {
            DockerContainerView(
                name = "apache2",
                dockerManager = httpServer.docker
            )
        }
        GeneralSpacer()
        NamedCheckbox(
            checked = enableVirtualHosts,
            onCheckedChange = { enableVirtualHosts = it },
            name = stringResource(Res.string.enable_virtual_hosts),
        )
        AnimatedVisibility(enableVirtualHosts) {
            VirtualHostsManager(httpServer)
        }
    }
}
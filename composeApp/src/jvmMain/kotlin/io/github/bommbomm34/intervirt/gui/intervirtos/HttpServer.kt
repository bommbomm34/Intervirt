package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.enable_virtual_hosts
import io.github.bommbomm34.intervirt.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.api.intervirtos.HttpServerManager
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.intervirtos.components.SystemServiceView
import io.github.bommbomm34.intervirt.gui.intervirtos.http.VirtualHostsManager
import io.github.bommbomm34.intervirt.rememberClient
import org.jetbrains.compose.resources.stringResource

@Composable
fun HttpServer(
    bundle: ContainerClientBundle
) {
    val httpServer = bundle.rememberClient(::HttpServerManager)
    var enableVirtualHosts by remember { mutableStateOf(false) }
    AlignedBox(Alignment.TopEnd) {
        SystemServiceView(
            serviceName = "apache2",
            serviceManager = httpServer.serviceManager
        )
    }
    GeneralSpacer()
    NamedCheckbox(
        checked = enableVirtualHosts,
        onCheckedChange = { enableVirtualHosts = it },
        name = stringResource(Res.string.enable_virtual_hosts)
    )
    AnimatedVisibility(enableVirtualHosts) {
        VirtualHostsManager(httpServer)
    }
}
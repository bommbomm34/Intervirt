package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.intervirtos.components.SystemServiceView

@Composable
fun SshServer(
    osClient: IntervirtOSClient
){
    SystemServiceView(
        serviceName = "ssh",
        serviceManager = osClient.serviceManager
    )
}
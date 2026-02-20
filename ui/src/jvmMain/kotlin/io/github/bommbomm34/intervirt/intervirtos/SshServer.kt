package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.intervirtos.components.SystemServiceView

@Composable
fun SshServer(
    osClient: IntervirtOSClient,
) {
    val client = osClient.getClient()
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.TopEnd) {
        _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.components.SystemServiceView(
            serviceName = "ssh",
            serviceManager = client.serviceManager,
        )
    }
}
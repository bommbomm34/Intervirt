package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.core.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.intervirtos.components.SystemServiceView

@Composable
fun SshServer(
    osClient: IntervirtOSClient,
) {
    val client = osClient.getClient()
    AlignedBox(Alignment.TopEnd) {
        SystemServiceView(
            serviceName = "ssh",
            serviceManager = client.serviceManager,
        )
    }
}
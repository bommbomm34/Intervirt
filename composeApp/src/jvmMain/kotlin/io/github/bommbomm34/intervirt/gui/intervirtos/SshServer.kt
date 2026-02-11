package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.intervirtos.components.SystemServiceView

@Composable
fun SshServer(
    osClient: IntervirtOSClient
) {
    AlignedBox(Alignment.TopEnd) {
        SystemServiceView(
            serviceName = "ssh",
            serviceManager = osClient.serviceManager
        )
    }
}
package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.intervirtos.components.SystemServiceView

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
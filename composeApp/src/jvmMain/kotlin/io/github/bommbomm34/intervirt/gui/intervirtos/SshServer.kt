package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.intervirtos.components.SystemServiceView
import io.github.bommbomm34.intervirt.rememberClient

@Composable
fun SshServer(
    bundle: ContainerClientBundle
) {
    AlignedBox(Alignment.TopEnd) {
        SystemServiceView(
            serviceName = "ssh",
            serviceManager = bundle.serviceManager
        )
    }
}
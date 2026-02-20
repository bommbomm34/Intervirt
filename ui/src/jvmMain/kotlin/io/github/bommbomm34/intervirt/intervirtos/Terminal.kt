package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.ShellView

@Composable
fun Terminal(
    osClient: IntervirtOSClient,
) {
    _root_ide_package_.io.github.bommbomm34.intervirt.components.ShellView(osClient.getClient().ioClient)
}
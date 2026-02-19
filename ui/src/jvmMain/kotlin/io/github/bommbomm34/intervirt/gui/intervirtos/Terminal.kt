package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.ShellView

@Composable
fun Terminal(
    osClient: IntervirtOSClient,
) {
    ShellView(osClient.getClient().ioClient)
}
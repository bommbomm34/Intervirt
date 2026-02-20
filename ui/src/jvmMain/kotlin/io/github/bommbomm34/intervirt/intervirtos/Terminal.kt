package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.components.ShellView

@Composable
fun Terminal(
    osClient: IntervirtOSClient,
) {
    ShellView(osClient.getClient().ioClient)
}
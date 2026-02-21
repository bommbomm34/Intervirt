package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.components.ShellView
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient

@Composable
fun Terminal(
    osClient: IntervirtOSClient,
) {
    ShellView(osClient.getClient().ioClient)
}
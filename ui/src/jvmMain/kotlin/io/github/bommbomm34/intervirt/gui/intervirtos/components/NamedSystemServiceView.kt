package io.github.bommbomm34.intervirt.gui.intervirtos.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.core.api.SystemServiceManager
import io.github.bommbomm34.intervirt.gui.components.CenterRow
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer

@Composable
fun NamedSystemServiceView(
    displayName: String,
    serviceName: String,
    serviceManager: SystemServiceManager,
) {
    CenterRow {
        Text(displayName)
        GeneralSpacer()
        SystemServiceView(serviceName, serviceManager)
    }
}
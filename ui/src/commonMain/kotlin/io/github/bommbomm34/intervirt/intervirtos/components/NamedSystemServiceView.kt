package io.github.bommbomm34.intervirt.intervirtos.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.core.api.SystemServiceManager
import io.github.bommbomm34.intervirt.components.CenterRow
import io.github.bommbomm34.intervirt.components.GeneralSpacer

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
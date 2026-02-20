package io.github.bommbomm34.intervirt.intervirtos.components

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
    _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterRow {
        Text(displayName)
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        SystemServiceView(serviceName, serviceManager)
    }
}
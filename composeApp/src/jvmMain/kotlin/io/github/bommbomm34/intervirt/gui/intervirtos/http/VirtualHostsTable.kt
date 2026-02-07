package io.github.bommbomm34.intervirt.gui.intervirtos.http

import androidx.compose.runtime.Composable
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.destination_folder
import intervirt.composeapp.generated.resources.domain
import io.github.bommbomm34.intervirt.data.VirtualHost
import io.github.bommbomm34.intervirt.gui.components.SimpleTable
import io.github.bommbomm34.intervirt.gui.components.buttons.RemoveButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun VirtualHostsTable(
    virtualHosts: List<VirtualHost>,
    onRemove: (VirtualHost) -> Unit
) {
    SimpleTable(
        headers = listOf(
            stringResource(Res.string.domain),
            stringResource(Res.string.destination_folder)
        ),
        content = virtualHosts.map { listOf(it.serverName, it.documentRoot) },
        customElements = virtualHosts.map {
            {
                RemoveButton { onRemove(it) }
            }
        }
    )
}
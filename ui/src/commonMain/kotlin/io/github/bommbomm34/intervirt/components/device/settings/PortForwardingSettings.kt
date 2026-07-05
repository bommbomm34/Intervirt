/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.add_port_forwarding
import intervirt.ui.generated.resources.remove_port_forwarding
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.TooltipArea
import io.github.bommbomm34.intervirt.components.buttons.AddButton
import io.github.bommbomm34.intervirt.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.currentAppEnv
import org.koin.compose.koinInject

@Composable
fun PortForwardingSettings(
    portForwardings: List<PortForwarding>,
    onAdd: () -> Unit,
    onRemove: (PortForwarding) -> Unit,
) {
    val appEnv = currentAppEnv
    val fabMod = remember { Modifier.size(appEnv.smallFabSize.dp) }
    Column {
        TooltipArea(Res.string.add_port_forwarding) {
            AddButton(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = fabMod,
                onClick = onAdd,
            )
        }
        if (portForwardings.isNotEmpty()) GeneralSpacer()
        LazyColumn {
            items(portForwardings) { portForwarding ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${portForwarding.protocol}:${portForwarding.internalPort}:${portForwarding.externalPort}")
                    GeneralSpacer(4.dp)
                    TooltipArea(Res.string.remove_port_forwarding) {
                        RemoveButton(fabMod) { onRemove(portForwarding) }
                    }
                }
            }
        }
    }
}

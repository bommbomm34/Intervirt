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
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.AddButton
import io.github.bommbomm34.intervirt.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.data.ViewDevice
import org.koin.compose.koinInject

@Composable
fun PortForwardingSettings(
    portForwardings: List<PortForwarding>,
    onAdd: () -> Unit,
    onRemove: (PortForwarding) -> Unit,
) {
    val appEnv = koinInject<AppEnv>()
    val fabMod = remember { Modifier.size(appEnv.SMALL_FAB_SIZE.dp) }
    Column {
        AddButton(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = fabMod,
            onClick = onAdd,
        )
        GeneralSpacer()
        LazyColumn {
            items(portForwardings) { portForwarding ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${portForwarding.protocol}:${portForwarding.internalPort}:${portForwarding.externalPort}")
                    GeneralSpacer(4.dp)
                    RemoveButton(fabMod) { onRemove(portForwarding) }
                }
            }
        }
    }
}
/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterRow
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
fun VMManagerView(
    running: Boolean,
    onBoot: () -> Unit,
    onReboot: () -> Unit,
    onSync: () -> Unit,
) {
    AlignedBox(Alignment.TopStart, padding = 16.dp) {
        CenterRow {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Shutdown button
                Button(onClick = onBoot) {
                    Text(stringResource(if (running) Res.string.shutdown else Res.string.boot))
                }
                GeneralSpacer(4.dp)
                // Reboot button
                Button(
                    onClick = onReboot,
                    enabled = running,
                ) {
                    Text(stringResource(Res.string.reboot))
                }
            }
            GeneralSpacer()
            // Sync button
            if (running) {
                IconButton(onClick = onSync) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(Res.string.sync_guest),
                    )
                }
            }
        }
    }
}